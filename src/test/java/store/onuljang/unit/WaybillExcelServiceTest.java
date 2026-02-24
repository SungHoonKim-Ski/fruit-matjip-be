package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import store.onuljang.courier.entity.CourierConfig;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.service.CourierConfigService;
import store.onuljang.courier.service.WaybillExcelService;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shop.admin.entity.Admin;

@ExtendWith(MockitoExtension.class)
class WaybillExcelServiceTest {

    @InjectMocks private WaybillExcelService waybillExcelService;
    @Mock private CourierConfigService courierConfigService;

    @BeforeEach
    void setUp() {
        CourierConfig config = CourierConfig.builder()
                .senderName("과일맛집")
                .senderPhone("010-0000-0000")
                .senderPhone2("02-1234-5678")
                .senderAddress("서울시 중구 명동길 1")
                .senderDetailAddress("1층")
                .build();
        given(courierConfigService.getConfig()).willReturn(config);
    }

    private CourierOrder createOrder(
            String displayCode, String receiverName, String receiverPhone) {
        Users user =
                Users.builder()
                        .socialId("social123")
                        .name("테스트유저")
                        .uid(UUID.randomUUID())
                        .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        Admin admin =
                Admin.builder()
                        .name("관리자")
                        .email("admin@test.com")
                        .password("password")
                        .build();

        CourierProduct product =
                CourierProduct.builder()
                        .name("제주 감귤 5kg")
                        .productUrl("https://example.com/img.jpg")
                        .price(new BigDecimal("15000"))
                        .visible(true)
                        .registeredAdmin(admin)
                        .build();
        ReflectionTestUtils.setField(product, "id", 1L);

        CourierOrder order =
                CourierOrder.builder()
                        .user(user)
                        .displayCode(displayCode)
                        .status(CourierOrderStatus.PAID)
                        .receiverName(receiverName)
                        .receiverPhone(receiverPhone)
                        .postalCode("06134")
                        .address1("서울시 강남구 테헤란로 123")
                        .address2("5층 501호")
                        .shippingMemo("부재 시 경비실에 맡겨주세요")
                        .productAmount(BigDecimal.valueOf(30000))
                        .shippingFee(BigDecimal.valueOf(4000))
                        .totalAmount(BigDecimal.valueOf(34000))
                        .build();
        ReflectionTestUtils.setField(order, "id", 1L);
        ReflectionTestUtils.setField(
                order, "createdAt", LocalDateTime.of(2026, 2, 22, 10, 30));

        CourierOrderItem item =
                CourierOrderItem.builder()
                        .courierOrder(order)
                        .courierProduct(product)
                        .productName("제주 감귤 5kg")
                        .productPrice(new BigDecimal("15000"))
                        .quantity(2)
                        .amount(new BigDecimal("30000"))
                        .build();
        order.getItems().add(item);

        return order;
    }

    @Test
    @DisplayName("단건 운송장 Excel 생성 - 유효한 xlsx 바이트 반환")
    void generateWaybillExcel_singleOrder_producesValidXlsx() throws Exception {
        // arrange
        CourierOrder order = createOrder("C-26022200-ABCD1", "홍길동", "010-1234-5678");

        // act
        byte[] result = waybillExcelService.generateWaybillExcel(order);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = wb.getSheetAt(0);
            Row dataRow = sheet.getRow(1);
            assertThat(dataRow).isNotNull();

            // 보내는 사람 정보 (0~4)
            assertThat(dataRow.getCell(0).getStringCellValue()).isEqualTo("과일맛집");
            assertThat(dataRow.getCell(1).getStringCellValue()).isEqualTo("010-0000-0000");
            assertThat(dataRow.getCell(2).getStringCellValue()).isEqualTo("02-1234-5678");
            assertThat(dataRow.getCell(3).getStringCellValue()).isEqualTo("서울시 중구 명동길 1");
            assertThat(dataRow.getCell(4).getStringCellValue()).isEqualTo("1층");

            // 받는 사람 정보 (5~9)
            assertThat(dataRow.getCell(5).getStringCellValue()).isEqualTo("홍길동");
            assertThat(dataRow.getCell(6).getStringCellValue()).isEqualTo("010-1234-5678");
            assertThat(dataRow.getCell(7).getStringCellValue()).isEqualTo("");
            assertThat(dataRow.getCell(8).getStringCellValue())
                    .isEqualTo("서울시 강남구 테헤란로 123");
            assertThat(dataRow.getCell(9).getStringCellValue()).isEqualTo("5층 501호");

            // 상품/주문 정보 (10~15)
            assertThat(dataRow.getCell(10).getStringCellValue()).isEqualTo("제주 감귤 5kg");
            assertThat(dataRow.getCell(11).getStringCellValue()).isEqualTo("");
            assertThat((int) dataRow.getCell(12).getNumericCellValue()).isEqualTo(2);
            assertThat(dataRow.getCell(13).getStringCellValue())
                    .isEqualTo("부재 시 경비실에 맡겨주세요");
            assertThat(dataRow.getCell(14).getStringCellValue()).isEqualTo("2026-02-22 10:30");
            assertThat(dataRow.getCell(15).getStringCellValue()).isEqualTo("C-26022200-ABCD1");
        }
    }

    @Test
    @DisplayName("복수 주문 운송장 Excel 생성 - 여러 행 생성")
    void generateWaybillExcel_multipleOrders_createsMultipleRows() throws Exception {
        // arrange
        CourierOrder order1 = createOrder("C-26022200-ABCD1", "홍길동", "010-1234-5678");
        CourierOrder order2 = createOrder("C-26022200-ABCD2", "김철수", "010-9876-5432");
        ReflectionTestUtils.setField(order2, "id", 2L);

        // act
        byte[] result = waybillExcelService.generateWaybillExcel(List.of(order1, order2));

        // assert
        assertThat(result).isNotNull();

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = wb.getSheetAt(0);

            Row row1 = sheet.getRow(1);
            assertThat(row1.getCell(0).getStringCellValue()).isEqualTo("과일맛집");
            assertThat(row1.getCell(5).getStringCellValue()).isEqualTo("홍길동");
            assertThat(row1.getCell(15).getStringCellValue()).isEqualTo("C-26022200-ABCD1");

            Row row2 = sheet.getRow(2);
            assertThat(row2.getCell(0).getStringCellValue()).isEqualTo("과일맛집");
            assertThat(row2.getCell(5).getStringCellValue()).isEqualTo("김철수");
            assertThat(row2.getCell(15).getStringCellValue()).isEqualTo("C-26022200-ABCD2");
        }
    }

    @Test
    @DisplayName("null 값이 있는 주문 - nullSafe 처리 확인")
    void generateWaybillExcel_nullFields_handledSafely() throws Exception {
        // arrange
        CourierOrder order = createOrder("C-26022200-ABCD1", "홍길동", "010-1234-5678");
        ReflectionTestUtils.setField(order, "address2", null);
        ReflectionTestUtils.setField(order, "shippingMemo", null);

        // act
        byte[] result = waybillExcelService.generateWaybillExcel(order);

        // assert
        assertThat(result).isNotNull();

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = wb.getSheetAt(0);
            Row dataRow = sheet.getRow(1);

            // receiver phone2 placeholder
            assertThat(dataRow.getCell(7).getStringCellValue()).isEqualTo("");
            // address2 (null → "")
            assertThat(dataRow.getCell(9).getStringCellValue()).isEqualTo("");
            // shippingMemo (null → "")
            assertThat(dataRow.getCell(13).getStringCellValue()).isEqualTo("");
        }
    }
}
