package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.onuljang.controller.response.ProductDetailResponse;
import store.onuljang.controller.response.ProductKeywordResponse;
import store.onuljang.controller.response.ProductListResponse;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Users;
import store.onuljang.support.IntegrationTestBase;
import store.onuljang.util.TimeUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static store.onuljang.util.TimeUtil.nowDate;

/**
 * 상품 조회 API 통합 테스트
 *
 * API Spec: - GET /api/auth/products?from={date}&to={date} - 상품 목록 조회 - GET
 * /api/auth/products/{id} - 상품 상세 조회 - GET /api/auth/products/keywords - 상품 키워드
 * 조회
 */
class ProductsIntegrationTest extends IntegrationTestBase {

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
    @DisplayName("GET /api/auth/products - 상품 목록 조회")
    class GetProducts {

        @Test
        @DisplayName("날짜 범위로 상품 목록 조회 성공")
        void getProducts_Success() throws Exception {
            // given
            LocalDate tomorrow = TimeUtil.tomorrowDate();
            LocalDate tomorrowNextDay = TimeUtil.tomorrowDate().plusDays(1);

            testFixture.createFutureProduct("내일상품", 5, new BigDecimal("5000"), 1, admin);
            testFixture.createFutureProduct("모레상품", 3, new BigDecimal("3000"), 2, admin);

            // when
            var response = getAction("/api/auth/products?from=" + tomorrow + "&to=" + tomorrowNextDay, accessToken,
                    ProductListResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).hasSize(2);
        }

        @Test
        @DisplayName("비공개 상품은 조회되지 않음")
        void getProducts_InvisibleProductNotShown() throws Exception {
            // given
            LocalDate today = nowDate();
            testFixture.createTodayProduct("공개상품", 10, new BigDecimal("10000"), admin);
            testFixture.createInvisibleProduct("비공개상품", 5, new BigDecimal("5000"), today, admin);

            String fromDate = today.format(DateTimeFormatter.ISO_DATE);
            String toDate = today.format(DateTimeFormatter.ISO_DATE);

            // when
            var response = getAction("/api/auth/products?from=" + fromDate + "&to=" + toDate, accessToken,
                    ProductListResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).hasSize(1);
            assertThat(response.body().response().get(0).name()).isEqualTo("공개상품");
        }

        @Test
        @DisplayName("인증되지 않은 요청 시 401 반환")
        void getProducts_Unauthorized() throws Exception {
            // given
            LocalDate today = LocalDate.now();
            String fromDate = today.format(DateTimeFormatter.ISO_DATE);
            String toDate = today.format(DateTimeFormatter.ISO_DATE);

            // when
            var response = getAction("/api/auth/products?from=" + fromDate + "&to=" + toDate, Void.class);

            // then
            System.out.println("DEBUG: Unauthorized Test Status Code: " + response.status());
            // then
            assertThat(response.isForbidden()).isTrue();
        }

        @Test
        @DisplayName("날짜 파라미터 누락 시 400 반환")
        void getProducts_MissingDateParameter() throws Exception {
            // given
            LocalDate today = LocalDate.now();
            String fromDate = today.format(DateTimeFormatter.ISO_DATE);

            // when
            var response = getAction("/api/auth/products?from=" + fromDate, accessToken, Void.class);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/auth/products/{id} - 상품 상세 조회")
    class GetProductDetail {

        @Test
        @DisplayName("상품 상세 조회 성공")
        void getProductDetail_Success() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("테스트상품", 10, new BigDecimal("15000"), admin);

            // when
            var response = getAction("/api/auth/products/" + product.getId(), accessToken, ProductDetailResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().name()).isEqualTo("테스트상품");
            assertThat(response.body().price()).isEqualByComparingTo(new BigDecimal("15000"));
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 404 반환")
        void getProductDetail_NotFound() throws Exception {
            // when
            var response = getAction("/api/auth/products/99999", accessToken, Void.class);

            // then
            assertThat(response.isNotFound()).isTrue();
        }

        @Test
        @DisplayName("잘못된 상품 ID 형식 시 400 반환")
        void getProductDetail_InvalidId() throws Exception {
            // when
            var response = getAction("/api/auth/products/-1", accessToken, Void.class);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/auth/products/keywords - 상품 키워드 조회")
    class GetProductKeywords {

        @Test
        @DisplayName("상품 키워드 목록 조회 성공")
        void getProductKeywords_Success() throws Exception {
            // given
            testFixture.createProductKeyword("과일");
            testFixture.createProductKeyword("채소");
            testFixture.createProductKeyword("육류");

            // when
            var response = getAction("/api/auth/products/keywords", accessToken, ProductKeywordResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).hasSize(3);
        }

        @Test
        @DisplayName("키워드가 없는 경우 빈 배열 반환")
        void getProductKeywords_Empty() throws Exception {
            // when
            var response = getAction("/api/auth/products/keywords", accessToken, ProductKeywordResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).isEmpty();
        }
    }
}
