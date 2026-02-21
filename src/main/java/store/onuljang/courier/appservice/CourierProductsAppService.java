package store.onuljang.courier.appservice;

import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.dto.CourierProductListResponse;
import store.onuljang.courier.dto.CourierProductResponse;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.service.CourierProductService;
import store.onuljang.shop.product.dto.ProductCategoryResponse;
import store.onuljang.shop.product.entity.ProductCategory;
import store.onuljang.shop.product.service.ProductCategoryService;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierProductsAppService {

    CourierProductService courierProductService;
    ProductCategoryService productCategoryService;

    public CourierProductListResponse getProducts(Long categoryId) {
        List<CourierProduct> products;
        if (categoryId != null) {
            products = courierProductService.findAllVisibleByCategory(categoryId);
        } else {
            products = courierProductService.findAllVisible();
        }
        return CourierProductListResponse.from(products);
    }

    public CourierProductResponse getDetail(Long id) {
        CourierProduct product = courierProductService.findByIdWithDetailImages(id);
        return CourierProductResponse.from(product);
    }

    public ProductCategoryResponse getProductCategories() {
        List<ProductCategory> categories = productCategoryService.findAllOrderBySortOrder();
        return ProductCategoryResponse.of(categories);
    }
}
