package store.onuljang.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import store.onuljang.courier.appservice.CourierAdminOrderAppService;
import store.onuljang.courier.dto.TrackingUploadResponse;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.repository.CourierOrderRepository;
import store.onuljang.courier.repository.CourierProductRepository;
import store.onuljang.courier.service.TrackingUploadException;
import store.onuljang.shared.entity.enums.CourierCompany;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.exception.AdminValidateException;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.support.IntegrationTestBase;

@DisplayName("운송장 업로드 통합 테스트")
class TrackingUploadIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CourierAdminOrderAppService courierAdminOrderAppService;

    @Autowired
    private CourierOrderRepository courierOrderRepository;

    @Autowired
    private CourierProductRepository courierProductRepository;

    @Autowired
    private EntityManager entityManager;

    private Users user;

    @BeforeEach
    void setUp() {
        user = testFixture.createUser("테스트유저");
    }

    // --- helper methods ---

    private CourierProduct saveCourierProduct(String name) {
        CourierProduct product = CourierProduct.builder()
                .name(name)
                .productUrl("https://example.com/image.jpg")
                .price(new BigDecimal("10000"))
                .sortOrder(0)
                .build();
        return courierProductRepository.save(product);
    }

    private CourierOrder saveCourierOrder(CourierOrderStatus status, String displayCode) {
        CourierOrder order = CourierOrder.builder()
                .user(user)
                .displayCode(displayCode)
                .status(status)
                .receiverName("홍길동")
                .receiverPhone("010-1234-5678")
                .postalCode("06134")
                .address1("서울시 강남구 테헤란로 1")
                .address2("101호")
                .productAmount(new BigDecimal("10000"))
                .shippingFee(new BigDecimal("3000"))
                .totalAmount(new BigDecimal("13000"))
                .build();
        return courierOrderRepository.save(order);
    }

    private void addOrderItem(CourierOrder order, CourierProduct product) {
        CourierOrderItem item = CourierOrderItem.builder()
                .courierOrder(order)
                .courierProduct(product)
                .productName(product.getName())
                .productPrice(product.getPrice())
                .quantity(1)
                .amount(product.getPrice())
                .build();
        entityManager.persist(item);
        order.getItems().add(item);
    }

    /**
     * 16열 포맷 엑셀 생성 헬퍼: 열 0 = 운송장번호, 열 15 = 주문번호
     */
    private MockMultipartFile createExcelFile(String[][] rows) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            var sheet = wb.createSheet("Sheet1");
            // 헤더 행 (row 0)
            sheet.createRow(0);
            // 데이터 행 (row 1~)
            for (int i = 0; i < rows.length; i++) {
                var row = sheet.createRow(i + 1);
                String[] cols = rows[i];
                if (cols.length > 0) {
                    row.createCell(0).setCellValue(cols[0]); // 운송장번호
                }
                if (cols.length > 1) {
                    row.createCell(15).setCellValue(cols[1]); // 주문번호
                }
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return new MockMultipartFile(
                    "file", "tracking.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    bos.toByteArray());
        }
    }

    private String uniqueDisplayCode() {
        return "C-" + UUID.randomUUID().toString().substring(0, 12);
    }

    @Nested
    @DisplayName("정상 업로드")
    class SuccessUpload {

        @Test
        @DisplayName("정상 업로드 시 운송장번호 저장 + SHIPPED 상태 전환")
        void 정상_업로드_운송장번호_저장_SHIPPED_전환() throws IOException {
            // Arrange
            String displayCode = uniqueDisplayCode();
            CourierProduct product = saveCourierProduct("상품A");
            CourierOrder order = saveCourierOrder(CourierOrderStatus.PAID, displayCode);
            addOrderItem(order, product);
            entityManager.flush();

            MockMultipartFile file = createExcelFile(new String[][]{
                {"1234567890", displayCode}
            });

            // Act
            TrackingUploadResponse response =
                    courierAdminOrderAppService.uploadTracking(file, CourierCompany.LOGEN);

            // Assert
            assertThat(response.updatedCount()).isEqualTo(1);

            entityManager.flush();
            entityManager.clear();
            CourierOrder updated = courierOrderRepository.findByDisplayCode(displayCode).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourierOrderStatus.SHIPPED);
            assertThat(updated.getWaybillNumber()).isEqualTo("1234567890");
            assertThat(updated.getCourierCompany()).isEqualTo(CourierCompany.LOGEN);
        }

        @Test
        @DisplayName("PREPARING 상태 주문도 SHIPPED 전환 가능")
        void PREPARING_상태_주문_SHIPPED_전환() throws IOException {
            // Arrange
            String displayCode = uniqueDisplayCode();
            CourierOrder order = saveCourierOrder(CourierOrderStatus.PREPARING, displayCode);
            entityManager.flush();

            MockMultipartFile file = createExcelFile(new String[][]{
                {"9876543210", displayCode}
            });

            // Act
            TrackingUploadResponse response =
                    courierAdminOrderAppService.uploadTracking(file, CourierCompany.HANJIN);

            // Assert
            assertThat(response.updatedCount()).isEqualTo(1);

            entityManager.flush();
            entityManager.clear();
            CourierOrder updated = courierOrderRepository.findByDisplayCode(displayCode).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourierOrderStatus.SHIPPED);
            assertThat(updated.getWaybillNumber()).isEqualTo("9876543210");
            assertThat(updated.getCourierCompany()).isEqualTo(CourierCompany.HANJIN);
        }

        @Test
        @DisplayName("여러 건 정상 업로드")
        void 여러건_정상_업로드() throws IOException {
            // Arrange
            String displayCode1 = uniqueDisplayCode();
            String displayCode2 = uniqueDisplayCode();
            saveCourierOrder(CourierOrderStatus.PAID, displayCode1);
            saveCourierOrder(CourierOrderStatus.PAID, displayCode2);
            entityManager.flush();

            MockMultipartFile file = createExcelFile(new String[][]{
                {"111111111", displayCode1},
                {"222222222", displayCode2}
            });

            // Act
            TrackingUploadResponse response =
                    courierAdminOrderAppService.uploadTracking(file, CourierCompany.LOGEN);

            // Assert
            assertThat(response.updatedCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("업로드 실패 케이스")
    class FailureUpload {

        @Test
        @DisplayName("존재하지 않는 displayCode → 전체 롤백 + TrackingUploadException")
        void 존재하지_않는_displayCode_전체_롤백() throws IOException {
            // Arrange
            String validCode = uniqueDisplayCode();
            String invalidCode = "C-NONEXISTENT00";
            saveCourierOrder(CourierOrderStatus.PAID, validCode);
            entityManager.flush();

            MockMultipartFile file = createExcelFile(new String[][]{
                {"111111111", validCode},
                {"222222222", invalidCode}
            });

            // Act & Assert
            assertThatThrownBy(() ->
                    courierAdminOrderAppService.uploadTracking(file, CourierCompany.LOGEN))
                    .isInstanceOf(TrackingUploadException.class)
                    .satisfies(e -> {
                        TrackingUploadException ex = (TrackingUploadException) e;
                        assertThat(ex.getErrors()).hasSize(1);
                        assertThat(ex.getErrors().get(0).displayCode()).isEqualTo(invalidCode);
                        assertThat(ex.getErrors().get(0).reason()).contains("존재하지 않는 주문번호");
                    });

            // 롤백 확인 — validCode 주문은 여전히 PAID
            entityManager.flush();
            entityManager.clear();
            CourierOrder notUpdated = courierOrderRepository.findByDisplayCode(validCode).orElseThrow();
            assertThat(notUpdated.getStatus()).isEqualTo(CourierOrderStatus.PAID);
            assertThat(notUpdated.getWaybillNumber()).isNull();
        }

        @Test
        @DisplayName("이미 운송장 등록된 주문 → TrackingUploadException")
        void 이미_운송장_등록된_주문_에러() throws IOException {
            // Arrange
            String displayCode = uniqueDisplayCode();
            CourierOrder order = saveCourierOrder(CourierOrderStatus.SHIPPED, displayCode);
            // 이미 운송장 등록된 상태 시뮬레이션
            order.markShipped("EXISTING-WAYBILL");
            entityManager.flush();

            MockMultipartFile file = createExcelFile(new String[][]{
                {"NEW-WAYBILL", displayCode}
            });

            // Act & Assert
            assertThatThrownBy(() ->
                    courierAdminOrderAppService.uploadTracking(file, CourierCompany.LOGEN))
                    .isInstanceOf(TrackingUploadException.class)
                    .satisfies(e -> {
                        TrackingUploadException ex = (TrackingUploadException) e;
                        assertThat(ex.getErrors()).hasSize(1);
                        assertThat(ex.getErrors().get(0).displayCode()).isEqualTo(displayCode);
                        assertThat(ex.getErrors().get(0).reason()).contains("이미 운송장이 등록된 주문");
                    });
        }

        @Test
        @DisplayName("운송장번호 빈값 → TrackingUploadException")
        void 운송장번호_빈값_에러() throws IOException {
            // Arrange
            String displayCode = uniqueDisplayCode();
            saveCourierOrder(CourierOrderStatus.PAID, displayCode);
            entityManager.flush();

            MockMultipartFile file = createExcelFile(new String[][]{
                {"", displayCode}
            });

            // Act & Assert
            assertThatThrownBy(() ->
                    courierAdminOrderAppService.uploadTracking(file, CourierCompany.LOGEN))
                    .isInstanceOf(TrackingUploadException.class)
                    .satisfies(e -> {
                        TrackingUploadException ex = (TrackingUploadException) e;
                        assertThat(ex.getErrors()).hasSize(1);
                        assertThat(ex.getErrors().get(0).reason()).contains("운송장번호");
                    });
        }

        @Test
        @DisplayName("빈 파일 → AdminValidateException")
        void 빈파일_에러() throws IOException {
            // Arrange
            MockMultipartFile emptyFile = createExcelFile(new String[][]{});

            // Act & Assert
            assertThatThrownBy(() ->
                    courierAdminOrderAppService.uploadTracking(emptyFile, CourierCompany.LOGEN))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("데이터가 없습니다");
        }

        @Test
        @DisplayName("null 파일 → AdminValidateException")
        void null_파일_에러() {
            // Arrange & Act & Assert
            assertThatThrownBy(() ->
                    courierAdminOrderAppService.uploadTracking(null, CourierCompany.LOGEN))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("파일이 없습니다");
        }
    }
}
