package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import store.onuljang.courier.appservice.CourierProductsAppService;
import store.onuljang.courier.dto.CourierProductListResponse;
import store.onuljang.courier.dto.CourierProductResponse;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.service.CourierProductService;
import store.onuljang.courier.dto.CourierCategoryResponse;
import store.onuljang.courier.entity.CourierProductCategory;
import store.onuljang.courier.service.CourierProductCategoryService;
import store.onuljang.shop.admin.entity.Admin;

@ExtendWith(MockitoExtension.class)
class CourierProductTest {

    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin =
                Admin.builder()
                        .name("테스트관리자")
                        .email("admin@test.com")
                        .password("password")
                        .build();
    }

    private CourierProduct createProduct(
            String name, BigDecimal price, boolean visible) {
        return CourierProduct.builder()
                .name(name)
                .productUrl("https://example.com/img.jpg")
                .price(price)
                .visible(visible)
                .registeredAdmin(testAdmin)
                .build();
    }

    // ===== 1. CourierProduct 도메인 메서드 테스트 =====

    @Nested
    @DisplayName("assertPurchasable - 구매 가능 검증")
    class AssertPurchasable {

        @Test
        @DisplayName("정상 구매 가능 상품")
        void purchasable_Success() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);

            // act & assert
            product.assertPurchasable(5);
        }

        @Test
        @DisplayName("비공개 상품 구매 불가")
        void purchasable_NotVisible() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), false);

            // act & assert
            assertThatThrownBy(() -> product.assertPurchasable(1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("판매가 중단된 상품");
        }

        @Test
        @DisplayName("삭제된 상품 구매 불가")
        void purchasable_Deleted() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);
            product.softDelete();

            // act & assert
            assertThatThrownBy(() -> product.assertPurchasable(1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("삭제된 상품");
        }

        @Test
        @DisplayName("수량이 0 이하일 때 예외")
        void purchasable_ZeroQuantity() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);

            // act & assert
            assertThatThrownBy(() -> product.assertPurchasable(0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("1개 이상");
        }
    }

    @Nested
    @DisplayName("purchase - 구매 처리")
    class Purchase {

        @Test
        @DisplayName("구매 시 판매량 증가")
        void purchase_Success() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);

            // act
            product.purchase(3);

            // assert
            assertThat(product.getTotalSold()).isEqualTo(3L);
        }

        @Test
        @DisplayName("연속 구매 시 누적 반영")
        void purchase_Multiple() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);

            // act
            product.purchase(3);
            product.purchase(2);

            // assert
            assertThat(product.getTotalSold()).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("restoreStock - 재고 복원")
    class RestoreStock {

        @Test
        @DisplayName("재고 복원 호출 시 예외 없이 정상 처리")
        void restoreStock_Success() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);

            // act & assert — restoreStock is a no-op; verify it runs without error
            product.restoreStock(3);
        }
    }

    @Nested
    @DisplayName("softDelete - 소프트 삭제")
    class SoftDelete {

        @Test
        @DisplayName("삭제 후 isAvailable false")
        void softDelete_NotAvailable() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);

            // act
            product.softDelete();

            // assert
            assertThat(product.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("toggleVisible - 노출 토글")
    class ToggleVisible {

        @Test
        @DisplayName("visible 토글 동작")
        void toggleVisible_Success() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);

            // act
            product.toggleVisible();

            // assert
            assertThat(product.getVisible()).isFalse();

            // act
            product.toggleVisible();

            // assert
            assertThat(product.getVisible()).isTrue();
        }
    }

    @Nested
    @DisplayName("isAvailable - 판매 가능 여부")
    class IsAvailable {

        @Test
        @DisplayName("visible=true, 미삭제이면 true")
        void isAvailable_True() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);

            // act & assert
            assertThat(product.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("visible=false 이면 false")
        void isAvailable_NotVisible() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), false);

            // act & assert
            assertThat(product.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("softDelete 후 false")
        void isAvailable_Deleted() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);
            product.softDelete();

            // act & assert
            assertThat(product.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("updateCategories - 카테고리 변경")
    class UpdateCategories {

        @Test
        @DisplayName("카테고리 교체 성공")
        void updateCategories_Success() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);
            CourierProductCategory cat1 =
                    CourierProductCategory.builder().name("과일").build();
            CourierProductCategory cat2 =
                    CourierProductCategory.builder().name("채소").build();

            // act
            product.updateCategories(Set.of(cat1, cat2));

            // assert
            assertThat(product.getProductCategories()).hasSize(2);
            assertThat(product.getProductCategories()).containsExactlyInAnyOrder(cat1, cat2);
        }

        @Test
        @DisplayName("null 전달 시 카테고리 비움")
        void updateCategories_Null() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);
            CourierProductCategory cat =
                    CourierProductCategory.builder().name("과일").build();
            product.updateCategories(Set.of(cat));

            // act
            product.updateCategories(null);

            // assert
            assertThat(product.getProductCategories()).isEmpty();
        }

        @Test
        @DisplayName("빈 Set 전달 시 카테고리 비움")
        void updateCategories_EmptySet() {
            // arrange
            CourierProduct product = createProduct("상품A", new BigDecimal("10000"), true);
            CourierProductCategory cat =
                    CourierProductCategory.builder().name("과일").build();
            product.updateCategories(Set.of(cat));

            // act
            product.updateCategories(Set.of());

            // assert
            assertThat(product.getProductCategories()).isEmpty();
        }
    }

    // ===== 2. CourierProductsAppService 테스트 =====

    @Nested
    @DisplayName("CourierProductsAppService")
    @ExtendWith(MockitoExtension.class)
    class CourierProductsAppServiceTest {

        @InjectMocks
        private CourierProductsAppService courierProductsAppService;

        @Mock
        private CourierProductService courierProductService;

        @Mock
        private CourierProductCategoryService courierProductCategoryService;

        @Test
        @DisplayName("전체 상품 목록 조회")
        void getProducts_All() {
            // arrange
            CourierProduct product1 =
                    createProduct("상품1", new BigDecimal("10000"), true);
            CourierProduct product2 =
                    createProduct("상품2", new BigDecimal("5000"), true);
            ReflectionTestUtils.setField(product1, "id", 1L);
            ReflectionTestUtils.setField(product2, "id", 2L);

            given(courierProductService.findAllVisible())
                    .willReturn(List.of(product1, product2));

            // act
            CourierProductListResponse result = courierProductsAppService.getProducts(null);

            // assert
            assertThat(result.response()).hasSize(2);
            assertThat(result.response().get(0).name()).isEqualTo("상품1");
            assertThat(result.response().get(1).name()).isEqualTo("상품2");
        }

        @Test
        @DisplayName("카테고리별 상품 목록 조회")
        void getProducts_ByCategory() {
            // arrange
            Long categoryId = 1L;
            CourierProduct product1 =
                    createProduct("상품1", new BigDecimal("10000"), true);
            ReflectionTestUtils.setField(product1, "id", 1L);

            given(courierProductService.findAllVisibleByCategory(categoryId))
                    .willReturn(List.of(product1));

            // act
            CourierProductListResponse result =
                    courierProductsAppService.getProducts(categoryId);

            // assert
            assertThat(result.response()).hasSize(1);
            assertThat(result.response().get(0).name()).isEqualTo("상품1");
        }

        @Test
        @DisplayName("상품 목록이 비어있을 때 빈 리스트 반환")
        void getProducts_Empty() {
            // arrange
            given(courierProductService.findAllVisible()).willReturn(List.of());

            // act
            CourierProductListResponse result = courierProductsAppService.getProducts(null);

            // assert
            assertThat(result.response()).isEmpty();
        }

        @Test
        @DisplayName("상품 상세 조회")
        void getDetail_Success() {
            // arrange
            CourierProduct product =
                    createProduct("상세상품", new BigDecimal("25000"), true);
            ReflectionTestUtils.setField(product, "id", 1L);

            given(courierProductService.findById(1L)).willReturn(product);

            // act
            CourierProductResponse result = courierProductsAppService.getDetail(1L);

            // assert
            assertThat(result.name()).isEqualTo("상세상품");
            assertThat(result.price()).isEqualTo(new BigDecimal("25000"));
        }

        @Test
        @DisplayName("카테고리 목록 조회")
        void getProductCategories_Success() {
            // arrange
            CourierProductCategory category1 =
                    CourierProductCategory.builder().name("과일").build();
            CourierProductCategory category2 =
                    CourierProductCategory.builder().name("채소").build();

            given(courierProductCategoryService.findAllOrderBySortOrder())
                    .willReturn(List.of(category1, category2));

            // act
            CourierCategoryResponse result =
                    courierProductsAppService.getProductCategories();

            // assert
            assertThat(result.response()).hasSize(2);
        }

        @Test
        @DisplayName("카테고리가 없는 경우 빈 목록 반환")
        void getProductCategories_Empty() {
            // arrange
            given(courierProductCategoryService.findAllOrderBySortOrder())
                    .willReturn(List.of());

            // act
            CourierCategoryResponse result =
                    courierProductsAppService.getProductCategories();

            // assert
            assertThat(result.response()).isEmpty();
        }
    }
}
