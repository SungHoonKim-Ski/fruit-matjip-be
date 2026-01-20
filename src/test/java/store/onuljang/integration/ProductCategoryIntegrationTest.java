package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.onuljang.controller.response.ProductCategoryResponse;
import store.onuljang.controller.response.ProductListResponse;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.ProductCategory;
import store.onuljang.repository.entity.Users;
import store.onuljang.support.IntegrationTestBase;
import store.onuljang.util.TimeUtil;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 상품 카테고리 API 통합 테스트
 *
 * API Spec: - GET /api/auth/products/categories - 카테고리 목록 조회 - GET
 * /api/auth/products?categoryId={id} - 카테고리별 상품 조회
 */
class ProductCategoryIntegrationTest extends IntegrationTestBase {

    private Users user;
    private String accessToken;
    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        user = testFixture.createUser("테스트유저");
        accessToken = testFixture.createAccessToken(user);
    }

    @Nested
    @DisplayName("GET /api/auth/products/categories - 카테고리 목록 조회")
    class GetProductCategories {

        @Test
        @DisplayName("카테고리 목록 조회 성공")
        void getProductCategories_Success() throws Exception {
            // given
            testFixture.createProductCategory("과일");
            testFixture.createProductCategory("채소");
            testFixture.createProductCategory("육류");

            // when
            var response = getAction("/api/auth/products/categories", accessToken, ProductCategoryResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).hasSize(3);
        }

        @Test
        @DisplayName("카테고리가 없는 경우 빈 배열 반환")
        void getProductCategories_Empty() throws Exception {
            // when
            var response = getAction("/api/auth/products/categories", accessToken, ProductCategoryResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).isEmpty();
        }

        @Test
        @DisplayName("카테고리에 id, name, imageUrl 포함")
        void getProductCategories_ContainsAllFields() throws Exception {
            // given
            testFixture.createProductCategory("과일", "https://example.com/fruit.jpg");

            // when
            var response = getAction("/api/auth/products/categories", accessToken, ProductCategoryResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).hasSize(1);
            var category = response.body().response().get(0);
            assertThat(category.id()).isNotNull();
            assertThat(category.name()).isEqualTo("과일");
            assertThat(category.imageUrl()).isEqualTo("https://example.com/fruit.jpg");
        }
    }

    @Nested
    @DisplayName("GET /api/auth/products?categoryId={id} - 카테고리별 상품 조회")
    class GetProductsByCategory {

        @Test
        @DisplayName("카테고리별 상품 조회 성공")
        void getProductsByCategory_Success() throws Exception {
            // given
            LocalDate tomorrow = TimeUtil.tomorrowDate();
            LocalDate dayAfterTomorrow = tomorrow.plusDays(1);

            ProductCategory fruitCategory = testFixture.createProductCategory("과일");
            ProductCategory vegetableCategory = testFixture.createProductCategory("채소");

            // 과일 카테고리 상품
            testFixture.createProductWithCategory("사과", 10, new BigDecimal("5000"), tomorrow, admin, fruitCategory);
            testFixture.createProductWithCategory("배", 5, new BigDecimal("6000"), tomorrow, admin, fruitCategory);

            // 채소 카테고리 상품
            testFixture.createProductWithCategory("당근", 20, new BigDecimal("3000"), tomorrow, admin, vegetableCategory);

            // when
            var response = getAction("/api/auth/products?from=" + tomorrow + "&to=" + dayAfterTomorrow + "&categoryId="
                    + fruitCategory.getId(), accessToken, ProductListResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).hasSize(2);
            assertThat(response.body().response()).extracting("name").containsExactlyInAnyOrder("사과", "배");
        }

        @Test
        @DisplayName("categoryId 없이 조회하면 전체 상품 반환")
        void getProducts_WithoutCategoryId_ReturnsAll() throws Exception {
            // given
            LocalDate tomorrow = TimeUtil.tomorrowDate();
            LocalDate dayAfterTomorrow = tomorrow.plusDays(1);

            ProductCategory fruitCategory = testFixture.createProductCategory("과일");

            testFixture.createProductWithCategory("사과", 10, new BigDecimal("5000"), tomorrow, admin, fruitCategory);
            testFixture.createFutureProduct("일반상품", 5, new BigDecimal("3000"), 1, admin);

            // when
            var response = getAction("/api/auth/products?from=" + tomorrow + "&to=" + dayAfterTomorrow, accessToken,
                    ProductListResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).hasSize(2);
        }

        @Test
        @DisplayName("카테고리에 상품이 없으면 빈 배열 반환")
        void getProductsByCategory_Empty() throws Exception {
            // given
            LocalDate tomorrow = TimeUtil.tomorrowDate();
            LocalDate dayAfterTomorrow = tomorrow.plusDays(1);

            ProductCategory emptyCategory = testFixture.createProductCategory("빈카테고리");
            testFixture.createFutureProduct("일반상품", 5, new BigDecimal("3000"), 1, admin);

            // when
            var response = getAction("/api/auth/products?from=" + tomorrow + "&to=" + dayAfterTomorrow + "&categoryId="
                    + emptyCategory.getId(), accessToken, ProductListResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).isEmpty();
        }

        @Test
        @DisplayName("상품이 여러 카테고리에 속할 수 있음")
        void product_CanBelongToMultipleCategories() throws Exception {
            // given
            LocalDate tomorrow = TimeUtil.tomorrowDate();
            LocalDate dayAfterTomorrow = tomorrow.plusDays(1);

            ProductCategory fruitCategory = testFixture.createProductCategory("과일");
            ProductCategory discountCategory = testFixture.createProductCategory("할인");

            // 과일 + 할인 카테고리에 모두 속하는 상품
            Product apple = testFixture.createProductWithCategory("사과", 10, new BigDecimal("5000"), tomorrow, admin,
                    fruitCategory);
            testFixture.addCategoryToProduct(apple, discountCategory);

            // 과일 카테고리로 조회
            var fruitResponse = getAction("/api/auth/products?from=" + tomorrow + "&to=" + dayAfterTomorrow
                    + "&categoryId=" + fruitCategory.getId(), accessToken, ProductListResponse.class);

            // 할인 카테고리로 조회
            var discountResponse = getAction("/api/auth/products?from=" + tomorrow + "&to=" + dayAfterTomorrow
                    + "&categoryId=" + discountCategory.getId(), accessToken, ProductListResponse.class);

            // then
            assertThat(fruitResponse.isOk()).isTrue();
            assertThat(fruitResponse.body().response()).hasSize(1);
            assertThat(fruitResponse.body().response().get(0).name()).isEqualTo("사과");

            assertThat(discountResponse.isOk()).isTrue();
            assertThat(discountResponse.body().response()).hasSize(1);
            assertThat(discountResponse.body().response().get(0).name()).isEqualTo("사과");
        }
    }
}
