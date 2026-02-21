package store.onuljang.shop.product.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shop.product.dto.ProductCategoryResponse;
import store.onuljang.shop.product.dto.ProductDetailResponse;
import store.onuljang.shop.product.dto.ProductListResponse;
import store.onuljang.shop.product.entity.Product;
import store.onuljang.shop.product.entity.ProductCategory;
import store.onuljang.shop.product.service.ProductCategoryService;
import store.onuljang.shop.product.service.ProductsService;

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
