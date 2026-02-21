package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store.onuljang.shop.admin.dto.*;
import store.onuljang.shop.delivery.dto.*;
import store.onuljang.shop.reservation.dto.*;
import store.onuljang.shared.auth.dto.*;
import store.onuljang.shop.admin.dto.AdminProductDetailResponse;
import store.onuljang.shop.admin.dto.AdminProductListItems;
import store.onuljang.shop.product.dto.ProductCategoryResponse;
import store.onuljang.shop.product.repository.ProductsRepository;
import store.onuljang.shop.product.repository.ProductCategoryRepository;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.product.entity.Product;
import store.onuljang.shop.product.entity.ProductCategory;
import store.onuljang.support.IntegrationTestBase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static store.onuljang.shared.util.TimeUtil.nowDate;

/**
 * 관리자 상품 관리 API 통합 테스트
 *
 * API Spec: - GET /api/admin/products - 전체 상품 조회 - GET
 * /api/admin/products/{productId} - 상품 상세 조회 - POST /api/admin/products - 상품 생성
 * - PATCH /api/admin/products/{productId} - 상품 수정 - DELETE
 * /api/admin/products/{productId} - 상품 삭제 - PATCH
 * /api/admin/products/visible/{productId} - 상품 노출 토글 - PATCH
 * /api/admin/products/self-pick/{productId} - 셀프 픽업 토글
 */
class AdminProductIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        setAdminAuthentication(admin);
    }

    @Nested
    @DisplayName("GET /api/admin/products - 전체 상품 조회")
    class GetAllProducts {

        @Test
        @DisplayName("전체 상품 조회 성공")
        void getAllProducts_Success() throws Exception {
            // given
            testFixture.createTodayProduct("상품1", 10, new BigDecimal("10000"), admin);
            testFixture.createTodayProduct("상품2", 5, new BigDecimal("5000"), admin);
            testFixture.createInvisibleProduct("비공개상품", 3, new BigDecimal("3000"), nowDate(), admin);

            // when
            var response = getAction("/api/admin/shop/products", AdminProductListItems.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/products/{productId} - 상품 상세 조회")
    class GetProductDetail {

        @Test
        @DisplayName("상품 상세 조회 성공")
        void getProductDetail_Success() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("테스트상품", 10, new BigDecimal("15000"), admin);

            // when
            var response = getAction("/api/admin/shop/products/" + product.getId(), AdminProductDetailResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().name()).isEqualTo("테스트상품");
            assertThat(response.body().price()).isEqualByComparingTo(new BigDecimal("15000"));
            assertThat(response.body().stock()).isEqualTo(10);
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 404 반환")
        void getProductDetail_NotFound() throws Exception {
            // when
            var response = getAction("/api/admin/shop/products/99999", Void.class);

            // then
            assertThat(response.isNotFound()).isTrue();
        }
    }

    @Nested
    @DisplayName("POST /api/admin/products - 상품 생성")
    class CreateProduct {

        @Test
        @DisplayName("상품 생성 성공")
        void createProduct_Success() throws Exception {
            // given
            AdminCreateProductRequest request = new AdminCreateProductRequest("새상품", new BigDecimal("25000"), 10,
                    "https://example.com/image.jpg", nowDate().plusDays(1).toString(), true, false);

            // when
            var response = postAction("/api/admin/shop/products", request, Long.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body()).isNotNull();

            // verify
            List<Product> products = productsRepository.findAll();
            assertThat(products).hasSize(1);
            assertThat(products.get(0).getName()).isEqualTo("새상품");
            assertThat(products.get(0).getProductCategories()).isEmpty();
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/products/{productId} - 상품 수정")
    class UpdateProduct {

        @Test
        @DisplayName("상품 수정 성공")
        void updateProduct_Success() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("기존상품", 10, new BigDecimal("10000"), admin);

            AdminUpdateProductDetailsRequest request = new AdminUpdateProductDetailsRequest("수정된상품",
                    new BigDecimal("20000"), 20, "https://example.com/new-image.jpg", nowDate().plusDays(2).toString(),
                    "수정된 설명", List.of(), LocalTime.of(12, 0), true);

            // when
            var response = patchAction("/api/admin/shop/products/" + product.getId(), request, Void.class);

            // then
            assertThat(response.isOk()).isTrue();

            // 수정 확인
            Product updatedProduct = productsRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getName()).isEqualTo("수정된상품");
            assertThat(updatedProduct.getStock()).isEqualTo(20);
            assertThat(updatedProduct.getProductCategories()).isEmpty();
        }

    }

    @Nested
    @DisplayName("DELETE /api/admin/products/{productId} - 상품 삭제")
    class DeleteProduct {

        @Test
        @DisplayName("상품 삭제 성공 (소프트 딜리트)")
        void deleteProduct_Success() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("삭제할상품", 10, new BigDecimal("10000"), admin);
            Long productId = product.getId();

            // when
            var response = deleteAction("/api/admin/shop/products/" + productId);

            // then
            assertThat(response.isOk()).isTrue();

            // 소프트 딜리트 확인 (findById는 삭제된 것을 반환하지 않음)
            entityManager.flush();
            entityManager.clear();
            assertThat(productsRepository.findById(productId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/products/visible/{productId} - 상품 노출 토글")
    class ToggleVisible {

        @Test
        @DisplayName("상품 노출 토글 성공")
        void toggleVisible_Success() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("테스트상품", 10, new BigDecimal("10000"), admin);
            boolean originalVisible = product.getVisible();

            // when
            var response = patchAction("/api/admin/shop/products/visible/" + product.getId());

            // then
            assertThat(response.isOk()).isTrue();

            // 토글 확인
            Product updatedProduct = productsRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getVisible()).isEqualTo(!originalVisible);
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/products/self-pick/{productId} - 셀프 픽업 토글")
    class ToggleSelfPick {

        @Test
        @DisplayName("셀프 픽업 토글 성공")
        void toggleSelfPick_Success() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("테스트상품", 10, new BigDecimal("10000"), admin);
            boolean originalSelfPick = product.getSelfPick();

            // when
            var response = patchAction("/api/admin/shop/products/self-pick/" + product.getId());

            // then
            assertThat(response.isOk()).isTrue();

            // 토글 확인
            Product updatedProduct = productsRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getSelfPick()).isEqualTo(!originalSelfPick);
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/products/bulk-sell-date - 판매일 일괄 변경")
    class BulkUpdateSellDate {

        @Test
        @DisplayName("판매일 일괄 변경 성공")
        void bulkUpdateSellDate_Success() throws Exception {
            // given
            Product product1 = testFixture.createTodayProduct("상품1", 10, new BigDecimal("10000"), admin);
            Product product2 = testFixture.createTodayProduct("상품2", 5, new BigDecimal("5000"), admin);
            LocalDate newSellDate = nowDate().plusDays(7);

            AdminProductBulkUpdateSellDateRequest request = new AdminProductBulkUpdateSellDateRequest(newSellDate,
                    Set.of(product1.getId(), product2.getId()));

            // when
            var response = patchAction("/api/admin/shop/products/bulk-sell-date", request, Integer.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/products/order - 상품 순서 변경")
    class UpdateProductOrder {

        @Test
        @DisplayName("상품 순서 변경 성공")
        void updateProductOrder_Success() throws Exception {
            // given
            Product product1 = testFixture.createTodayProduct("상품1", 10, new BigDecimal("10000"), admin);
            Product product2 = testFixture.createTodayProduct("상품2", 5, new BigDecimal("5000"), admin);
            Product product3 = testFixture.createTodayProduct("상품3", 3, new BigDecimal("3000"), admin);

            AdminProductUpdateOrderRequest request = new AdminProductUpdateOrderRequest(
                    List.of(product3.getId(), product1.getId(), product2.getId()));

            // when
            var response = patchAction("/api/admin/shop/products/order", request, Integer.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("상품 카테고리 관리")
    class ProductCategoryManagement {

        @Test
        @DisplayName("GET /api/admin/products/categories - 카테고리 목록 조회")
        void getProductCategories_Success() throws Exception {
            // given
            testFixture.createProductCategory("과일");
            testFixture.createProductCategory("채소");

            // when
            var response = getAction("/api/admin/shop/products/categories", ProductCategoryResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).hasSize(2);
        }

        @Test
        @DisplayName("POST /api/admin/products/category - 카테고리 추가")
        void saveProductCategory_Success() throws Exception {
            // given
            AdminCreateCategoryRequest request = new AdminCreateCategoryRequest("새카테고리",
                    "https://example.com/image.jpg");

            // when
            var response = postAction("/api/admin/shop/products/category", request, Void.class);

            // then
            assertThat(response.isOk()).isTrue();
        }

        @Test
        @DisplayName("DELETE /api/admin/products/category - 카테고리 삭제")
        void deleteProductCategory_Success() throws Exception {
            // given
            testFixture.createProductCategory("삭제할카테고리");

            // when
            var response = deleteAction("/api/admin/shop/products/category?keyword=삭제할카테고리");

            // then
            assertThat(response.isOk()).isTrue();
        }

        @Test
        @DisplayName("PUT /api/admin/products/categories/{categoryId}/products - 카테고리에 속한 상품 목록 수정")
        void updateCategoryProducts_Success() throws Exception {
            // given
            var category = testFixture.createProductCategory("테스트카테고리");
            Product p1 = testFixture.createTodayProduct("상품1", 10, new BigDecimal("10000"), admin);
            Product p2 = testFixture.createTodayProduct("상품2", 5, new BigDecimal("5000"), admin);

            AdminCategoryProductsRequest request = new AdminCategoryProductsRequest(List.of(p1.getId(), p2.getId()));

            // when
            var response = putAction("/api/admin/shop/products/categories/" + category.getId() + "/products", request);

            // then
            assertThat(response.isOk()).isTrue();

            // verify
            entityManager.flush();
            entityManager.clear();
            List<Product> products = productsRepository.findAllByCategoryId(category.getId());
            assertThat(products).hasSize(2);
        }

        @Test
        @DisplayName("POST /api/admin/products/category - 중복 카테고리명 생성 시 실패")
        void saveProductCategory_DuplicateName_Fail() throws Exception {
            // given
            testFixture.createProductCategory("과일");
            AdminCreateCategoryRequest request = new AdminCreateCategoryRequest("과일",
                    "https://example.com/image.jpg");

            // when
            var response = postAction("/api/admin/shop/products/category", request, Void.class);

            // then
            assertThat(response.isOk()).isFalse();
        }

        @Test
        @DisplayName("POST /api/admin/products/category - 카테고리명 5자 초과 시 실패")
        void saveProductCategory_NameTooLong_Fail() throws Exception {
            // given
            AdminCreateCategoryRequest request = new AdminCreateCategoryRequest("여섯글자카테",
                    "https://example.com/image.jpg");

            // when
            var response = postAction("/api/admin/shop/products/category", request, Void.class);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }

        @Test
        @DisplayName("PATCH /api/admin/products/category/{categoryId} - 카테고리 수정 성공")
        void updateProductCategory_Success() throws Exception {
            // given
            var category = testFixture.createProductCategory("기존이름");
            AdminUpdateCategoryRequest request = new AdminUpdateCategoryRequest("수정이름",
                    "https://example.com/new-image.jpg");

            // when
            var response = patchAction("/api/admin/shop/products/category/" + category.getId(), request, Void.class);

            // then
            assertThat(response.isOk()).isTrue();

            // verify
            entityManager.flush();
            entityManager.clear();
            ProductCategory updated = productCategoryRepository.findById(category.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("수정이름");
            assertThat(updated.getImageUrl()).isEqualTo("https://example.com/new-image.jpg");
        }

        @Test
        @DisplayName("PATCH /api/admin/products/category/{categoryId} - 존재하지 않는 카테고리 수정 시 실패")
        void updateProductCategory_NotFound_Fail() throws Exception {
            // given
            AdminUpdateCategoryRequest request = new AdminUpdateCategoryRequest("수정", null);

            // when
            var response = patchAction("/api/admin/shop/products/category/99999", request, Void.class);

            // then
            assertThat(response.isOk()).isFalse();
        }

        @Test
        @DisplayName("PATCH /api/admin/products/categories/order - 카테고리 순서 변경 성공")
        void updateCategorySortOrders_Success() throws Exception {
            // given
            var cat1 = testFixture.createProductCategory("과일");
            var cat2 = testFixture.createProductCategory("채소");
            var cat3 = testFixture.createProductCategory("육류");

            AdminUpdateCategoryListRequest request = new AdminUpdateCategoryListRequest(List.of(
                    new AdminUpdateCategoryListRequest.CategoryItemRequest(cat3.getId(), null, null),
                    new AdminUpdateCategoryListRequest.CategoryItemRequest(cat1.getId(), null, null),
                    new AdminUpdateCategoryListRequest.CategoryItemRequest(cat2.getId(), null, null)));

            // when
            var response = patchAction("/api/admin/shop/products/categories/order", request, Void.class);

            // then
            assertThat(response.isOk()).isTrue();

            // verify - sortOrder 순으로 조회하면 cat3, cat1, cat2 순
            entityManager.flush();
            entityManager.clear();
            var categories = productCategoryRepository.findAllByOrderBySortOrderAsc();
            assertThat(categories).extracting("name").containsExactly("육류", "과일", "채소");
        }

        @Test
        @DisplayName("POST /api/admin/products/{productId}/categories/{categoryId} - 상품에 카테고리 추가")
        void addCategoryToProduct_Success() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("상품", 10, new BigDecimal("10000"), admin);
            var category = testFixture.createProductCategory("과일");

            // when
            var response = postAction("/api/admin/shop/products/" + product.getId() + "/categories/" + category.getId(),
                    "", Void.class);

            // then
            assertThat(response.isOk()).isTrue();

            // verify
            entityManager.flush();
            entityManager.clear();
            List<Product> products = productsRepository.findAllByCategoryId(category.getId());
            assertThat(products).hasSize(1);
            assertThat(products.get(0).getName()).isEqualTo("상품");
        }

        @Test
        @DisplayName("DELETE /api/admin/products/{productId}/categories/{categoryId} - 상품에서 카테고리 제거")
        void removeCategoryFromProduct_Success() throws Exception {
            // given
            var category = testFixture.createProductCategory("과일");
            Product product = testFixture.createTodayProduct("상품", 10, new BigDecimal("10000"), admin);
            testFixture.addCategoryToProduct(product, category);

            // when
            var response = deleteAction(
                    "/api/admin/shop/products/" + product.getId() + "/categories/" + category.getId());

            // then
            assertThat(response.isOk()).isTrue();

            // verify
            entityManager.flush();
            entityManager.clear();
            List<Product> products = productsRepository.findAllByCategoryId(category.getId());
            assertThat(products).isEmpty();
        }

        @Test
        @DisplayName("PUT /api/admin/products/categories/{categoryId}/products - 빈 목록으로 수정하면 상품 전체 해제")
        void updateCategoryProducts_EmptyList_ClearsAll() throws Exception {
            // given
            var category = testFixture.createProductCategory("테스트");
            Product p1 = testFixture.createTodayProduct("상품1", 10, new BigDecimal("10000"), admin);
            testFixture.addCategoryToProduct(p1, category);

            AdminCategoryProductsRequest request = new AdminCategoryProductsRequest(Collections.emptyList());

            // when
            var response = putAction("/api/admin/shop/products/categories/" + category.getId() + "/products", request);

            // then
            assertThat(response.isOk()).isTrue();

            // verify
            entityManager.flush();
            entityManager.clear();
            List<Product> products = productsRepository.findAllByCategoryId(category.getId());
            assertThat(products).isEmpty();
        }
    }
}
