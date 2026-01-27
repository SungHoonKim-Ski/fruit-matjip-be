package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.response.ProductCategoryResponse;
import store.onuljang.controller.response.ProductDetailResponse;
import store.onuljang.controller.response.ProductListResponse;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.ProductCategory;
import store.onuljang.service.ProductCategoryService;
import store.onuljang.service.ProductsService;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductsAppService {
    ProductsService productsService;
    ProductCategoryService productCategoryService;

    @Transactional(readOnly = true)
    public ProductCategoryResponse getProductCategories() {
        List<ProductCategory> categories = productCategoryService.findAllOrderBySortOrder();
        return ProductCategoryResponse.of(categories);
    }

    @Transactional(readOnly = true)
    public ProductListResponse getProducts(LocalDate from, LocalDate to, Long categoryId) {
        List<Product> products;
        if (categoryId != null) {
            products = productsService.findAllVisibleBetweenByCategory(from, to, true, categoryId);
        } else {
            products = productsService.findAllVisibleBetween(from, to, true);
        }
        return ProductListResponse.from(products);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(long id) {
        Product products = productsService.findByIdWithDetailImages(id);
        return ProductDetailResponse.from(products);
    }
}
