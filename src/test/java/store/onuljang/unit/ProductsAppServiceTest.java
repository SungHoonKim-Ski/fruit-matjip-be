package store.onuljang.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.onuljang.appservice.ProductsAppService;
import store.onuljang.controller.response.ProductCategoryResponse;
import store.onuljang.controller.response.ProductDetailResponse;
import store.onuljang.controller.response.ProductListResponse;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.ProductCategory;
import store.onuljang.service.ProductCategoryService;
import store.onuljang.service.ProductsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * ProductsAppService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class ProductsAppServiceTest {

    @InjectMocks
    private ProductsAppService productsAppService;

    @Mock
    private ProductsService productsService;

    @Mock
    private ProductCategoryService productCategoryService;

    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = Admin.builder().name("테스트관리자").email("admin@test.com").password("password").build();
    }

    @Nested
    @DisplayName("getProducts - 상품 목록 조회")
    class GetProducts {

        @Test
        @DisplayName("날짜 범위로 공개 상품 목록 조회")
        void getProducts_ReturnsVisibleProducts() {
            // given
            LocalDate from = LocalDate.now();
            LocalDate to = LocalDate.now().plusDays(2);

            Product product1 = Product.builder().name("상품1").stock(10).price(new BigDecimal("10000"))
                    .sellDate(LocalDate.now()).productUrl("https://example.com/img1.jpg").visible(true).selfPick(true)
                    .registeredAdmin(testAdmin).build();

            Product product2 = Product.builder().name("상품2").stock(5).price(new BigDecimal("5000"))
                    .sellDate(LocalDate.now().plusDays(1)).productUrl("https://example.com/img2.jpg").visible(true)
                    .selfPick(true).registeredAdmin(testAdmin).build();

            given(productsService.findAllVisibleBetween(from, to, true)).willReturn(List.of(product1, product2));

            // when
            ProductListResponse result = productsAppService.getProducts(from, to, null);

            // then
            assertThat(result.response()).hasSize(2);
            assertThat(result.response().get(0).name()).isEqualTo("상품1");
            assertThat(result.response().get(1).name()).isEqualTo("상품2");
        }

        @Test
        @DisplayName("상품이 없는 경우 빈 목록 반환")
        void getProducts_ReturnsEmptyList() {
            // given
            LocalDate from = LocalDate.now();
            LocalDate to = LocalDate.now();

            given(productsService.findAllVisibleBetween(from, to, true)).willReturn(List.of());

            // when
            ProductListResponse result = productsAppService.getProducts(from, to, null);

            // then
            assertThat(result.response()).isEmpty();
        }

        @Test
        @DisplayName("카테고리별 상품 목록 조회")
        void getProducts_ByCategoryId() {
            // given
            LocalDate from = LocalDate.now();
            LocalDate to = LocalDate.now().plusDays(2);
            Long categoryId = 1L;

            Product product1 = Product.builder().name("상품1").stock(10).price(new BigDecimal("10000"))
                    .sellDate(LocalDate.now()).productUrl("https://example.com/img1.jpg").visible(true).selfPick(true)
                    .registeredAdmin(testAdmin).build();

            given(productsService.findAllVisibleBetweenByCategory(from, to, true, categoryId))
                    .willReturn(List.of(product1));

            // when
            ProductListResponse result = productsAppService.getProducts(from, to, categoryId);

            // then
            assertThat(result.response()).hasSize(1);
            assertThat(result.response().get(0).name()).isEqualTo("상품1");
        }
    }

    @Nested
    @DisplayName("getDetail - 상품 상세 조회")
    class GetDetail {

        @Test
        @DisplayName("상품 상세 정보 조회 성공")
        void getDetail_Success() {
            // given
            Product product = Product.builder().name("상세상품").stock(15).price(new BigDecimal("25000"))
                    .sellDate(LocalDate.now()).productUrl("https://example.com/detail.jpg").visible(true).selfPick(true)
                    .registeredAdmin(testAdmin).description("상품 상세 설명").build();

            given(productsService.findByIdWithDetailImages(1L)).willReturn(product);

            // when
            ProductDetailResponse result = productsAppService.getDetail(1L);

            // then
            assertThat(result.name()).isEqualTo("상세상품");
            assertThat(result.price()).isEqualTo(new BigDecimal("25000"));
        }
    }

    @Nested
    @DisplayName("getProductCategories - 상품 카테고리 조회")
    class GetProductCategories {

        @Test
        @DisplayName("상품 카테고리 목록 조회 성공")
        void getProductCategories_Success() {
            // given
            ProductCategory category1 = ProductCategory.builder().name("과일").build();
            ProductCategory category2 = ProductCategory.builder().name("채소").build();

            given(productCategoryService.findAllOrderBySortOrder()).willReturn(List.of(category1, category2));

            // when
            ProductCategoryResponse result = productsAppService.getProductCategories();

            // then
            assertThat(result.response()).hasSize(2);
        }

        @Test
        @DisplayName("카테고리가 없는 경우 빈 목록 반환")
        void getProductCategories_Empty() {
            // given
            given(productCategoryService.findAllOrderBySortOrder()).willReturn(List.of());

            // when
            ProductCategoryResponse result = productsAppService.getProductCategories();

            // then
            assertThat(result.response()).isEmpty();
        }
    }
}
