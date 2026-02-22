package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import store.onuljang.courier.appservice.CourierAdminProductAppService;
import store.onuljang.courier.dto.CourierProductCreateRequest;
import store.onuljang.courier.dto.CourierProductListResponse;
import store.onuljang.courier.dto.CourierProductResponse;
import store.onuljang.courier.dto.CourierProductUpdateRequest;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.service.CourierProductService;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.admin.service.AdminService;
import store.onuljang.shop.admin.service.AdminUploadService;
import store.onuljang.shop.admin.util.SessionUtil;
import store.onuljang.courier.entity.CourierProductCategory;
import store.onuljang.courier.repository.CourierProductRepository;
import store.onuljang.courier.service.CourierProductCategoryService;

@ExtendWith(MockitoExtension.class)
class CourierAdminProductAppServiceTest {

    @InjectMocks private CourierAdminProductAppService courierAdminProductAppService;

    @Mock private CourierProductService courierProductService;
    @Mock private CourierProductRepository courierProductRepository;
    @Mock private CourierProductCategoryService courierProductCategoryService;
    @Mock private AdminUploadService adminUploadService;
    @Mock private AdminService adminService;

    private Admin testAdmin;
    private MockedStatic<SessionUtil> sessionUtilMock;

    @BeforeEach
    void setUp() {
        testAdmin =
                Admin.builder()
                        .name("관리자")
                        .email("admin@test.com")
                        .password("password")
                        .build();
        ReflectionTestUtils.setField(testAdmin, "id", 1L);

        sessionUtilMock = Mockito.mockStatic(SessionUtil.class);
        sessionUtilMock.when(SessionUtil::getAdminId).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        sessionUtilMock.close();
    }

    private CourierProduct createProduct(String name, int stock) {
        CourierProduct product =
                CourierProduct.builder()
                        .name(name)
                        .productUrl("https://example.com/img.jpg")
                        .price(new BigDecimal("15000"))
                        .stock(stock)
                        .visible(true)
                        .registeredAdmin(testAdmin)
                        .build();
        ReflectionTestUtils.setField(product, "id", 1L);
        return product;
    }

    // --- getProducts ---

    @Nested
    @DisplayName("getProducts - 관리자 상품 목록 조회")
    class GetProducts {

        @Test
        @DisplayName("전체 상품 목록을 반환한다")
        void getProducts_success() {
            // arrange
            CourierProduct product1 = createProduct("감귤", 10);
            CourierProduct product2 = createProduct("한라봉", 5);
            ReflectionTestUtils.setField(product2, "id", 2L);

            given(courierProductService.findAll()).willReturn(List.of(product1, product2));

            // act
            CourierProductListResponse result = courierAdminProductAppService.getProducts();

            // assert
            assertThat(result.response()).hasSize(2);
            assertThat(result.response().get(0).name()).isEqualTo("감귤");
            assertThat(result.response().get(1).name()).isEqualTo("한라봉");
        }
    }

    // --- getProduct ---

    @Nested
    @DisplayName("getProduct - 관리자 상품 상세 조회")
    class GetProduct {

        @Test
        @DisplayName("상세 이미지 포함 단일 상품을 반환한다")
        void getProduct_success() {
            // arrange
            CourierProduct product = createProduct("감귤", 10);
            product.replaceDetailImages(List.of("img1.jpg", "img2.jpg"));

            given(courierProductService.findByIdWithDetailImages(1L)).willReturn(product);

            // act
            CourierProductResponse result = courierAdminProductAppService.getProduct(1L);

            // assert
            assertThat(result.name()).isEqualTo("감귤");
            assertThat(result.detailImageUrls()).hasSize(2);
            assertThat(result.detailImageUrls().get(0)).isEqualTo("img1.jpg");
        }
    }

    // --- createProduct ---

    @Nested
    @DisplayName("createProduct - 상품 생성")
    class CreateProduct {

        @Test
        @DisplayName("카테고리와 상세 이미지 포함 상품 생성 성공")
        void createProduct_withCategoriesAndImages() {
            // arrange
            CourierProductCategory category =
                    CourierProductCategory.builder().name("과일").build();
            ReflectionTestUtils.setField(category, "id", 10L);

            CourierProductCreateRequest request =
                    new CourierProductCreateRequest(
                            "감귤",
                            "https://example.com/img.jpg",
                            new BigDecimal("15000"),
                            10,
                            500,
                            "제주 감귤",
                            1,
                            List.of(10L),
                            List.of("detail1.jpg", "detail2.jpg"),
                            null,
                            null);

            given(adminService.findById(1L)).willReturn(testAdmin);
            given(courierProductCategoryService.findById(10L))
                    .willReturn(Optional.of(category));
            given(courierProductService.save(any(CourierProduct.class))).willReturn(1L);

            // act
            CourierProductResponse result = courierAdminProductAppService.createProduct(request);

            // assert
            assertThat(result.name()).isEqualTo("감귤");
            assertThat(result.price()).isEqualByComparingTo(new BigDecimal("15000"));
            assertThat(result.stock()).isEqualTo(10);
            assertThat(result.detailImageUrls()).hasSize(2);
            verify(courierProductService).save(any(CourierProduct.class));
        }

        @Test
        @DisplayName("카테고리 없이 상품 생성 성공")
        void createProduct_withoutCategories() {
            // arrange
            CourierProductCreateRequest request =
                    new CourierProductCreateRequest(
                            "한라봉",
                            "https://example.com/img.jpg",
                            new BigDecimal("25000"),
                            5,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);

            given(adminService.findById(1L)).willReturn(testAdmin);
            given(courierProductRepository.findMinSortOrder()).willReturn(0);
            given(courierProductService.save(any(CourierProduct.class))).willReturn(1L);

            // act
            CourierProductResponse result = courierAdminProductAppService.createProduct(request);

            // assert
            assertThat(result.name()).isEqualTo("한라봉");
            assertThat(result.categories()).isEmpty();
            assertThat(result.detailImageUrls()).isEmpty();
            verify(courierProductService).save(any(CourierProduct.class));
        }
    }

    // --- updateProduct ---

    @Nested
    @DisplayName("updateProduct - 상품 수정")
    class UpdateProduct {

        @Test
        @DisplayName("이름, 가격, 재고 필드 수정 성공")
        void updateProduct_fieldsUpdate() {
            // arrange
            CourierProduct product = createProduct("감귤", 10);

            CourierProductUpdateRequest request =
                    new CourierProductUpdateRequest(
                            "제주 감귤",
                            null,
                            new BigDecimal("18000"),
                            20,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);

            given(courierProductService.findByIdWithDetailImages(1L)).willReturn(product);

            // act
            CourierProductResponse result =
                    courierAdminProductAppService.updateProduct(1L, request);

            // assert
            assertThat(result.name()).isEqualTo("제주 감귤");
            assertThat(result.price()).isEqualByComparingTo(new BigDecimal("18000"));
            assertThat(result.stock()).isEqualTo(20);
        }

        @Test
        @DisplayName("visible true -> false 토글 수정")
        void updateProduct_visibleToggle() {
            // arrange
            CourierProduct product = createProduct("감귤", 10);
            assertThat(product.getVisible()).isTrue();

            CourierProductUpdateRequest request =
                    new CourierProductUpdateRequest(
                            null, null, null, null, null, null, null, false, null, null, null, null);

            given(courierProductService.findByIdWithDetailImages(1L)).willReturn(product);

            // act
            CourierProductResponse result =
                    courierAdminProductAppService.updateProduct(1L, request);

            // assert
            assertThat(result.visible()).isFalse();
        }

        @Test
        @DisplayName("카테고리 변경 성공")
        void updateProduct_categoriesUpdate() {
            // arrange
            CourierProduct product = createProduct("감귤", 10);

            CourierProductCategory category =
                    CourierProductCategory.builder().name("과일").build();
            ReflectionTestUtils.setField(category, "id", 10L);

            CourierProductUpdateRequest request =
                    new CourierProductUpdateRequest(
                            null, null, null, null, null, null, null, null, List.of(10L), null,
                            null, null);

            given(courierProductService.findByIdWithDetailImages(1L)).willReturn(product);
            given(courierProductCategoryService.findById(10L))
                    .willReturn(Optional.of(category));

            // act
            CourierProductResponse result =
                    courierAdminProductAppService.updateProduct(1L, request);

            // assert
            assertThat(product.getProductCategories()).isEqualTo(Set.of(category));
        }
    }

    // --- deleteProduct ---

    @Nested
    @DisplayName("deleteProduct - 상품 삭제")
    class DeleteProduct {

        @Test
        @DisplayName("softDelete 호출")
        void deleteProduct_callsSoftDelete() {
            // arrange
            CourierProduct product = createProduct("감귤", 10);

            given(courierProductService.findById(1L)).willReturn(product);

            // act
            courierAdminProductAppService.deleteProduct(1L);

            // assert
            assertThat(product.isAvailable()).isFalse();
        }
    }

    // --- toggleVisible ---

    @Nested
    @DisplayName("toggleVisible - 노출 토글")
    class ToggleVisible {

        @Test
        @DisplayName("잠금 획득 후 toggleVisible 호출")
        void toggleVisible_callsWithLock() {
            // arrange
            CourierProduct product = createProduct("감귤", 10);
            assertThat(product.getVisible()).isTrue();

            given(courierProductService.findByIdWithLock(1L)).willReturn(product);

            // act
            courierAdminProductAppService.toggleVisible(1L);

            // assert
            assertThat(product.getVisible()).isFalse();
            verify(courierProductService).findByIdWithLock(1L);
        }
    }
}
