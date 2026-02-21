package store.onuljang.courier.appservice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.dto.CourierProductListResponse;
import store.onuljang.courier.dto.CourierProductResponse;
import store.onuljang.courier.dto.CourierProductsByCategoryResponse;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.dto.CourierCategoryResponse;
import store.onuljang.courier.entity.CourierProductCategory;
import store.onuljang.courier.service.CourierProductCategoryService;
import store.onuljang.courier.service.CourierProductService;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierProductsAppService {

    CourierProductService courierProductService;
    CourierProductCategoryService courierProductCategoryService;

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

    public CourierCategoryResponse getProductCategories() {
        List<CourierProductCategory> categories =
                courierProductCategoryService.findAllOrderBySortOrder();
        return CourierCategoryResponse.of(categories);
    }

    public CourierProductListResponse getRecommendedProducts(int limit) {
        List<CourierProduct> recommended = courierProductService.findRecommendedProducts();

        if (recommended.size() < limit) {
            Set<Long> recommendedIds = recommended.stream()
                    .map(CourierProduct::getId).collect(Collectors.toSet());
            List<CourierProduct> allVisible = courierProductService.findAllVisible();
            List<CourierProduct> byTotalSold = allVisible.stream()
                    .filter(p -> !recommendedIds.contains(p.getId()))
                    .sorted(Comparator.comparingLong(CourierProduct::getTotalSold).reversed())
                    .limit(limit - recommended.size())
                    .toList();
            List<CourierProduct> combined = new ArrayList<>(recommended);
            combined.addAll(byTotalSold);
            return CourierProductListResponse.from(combined);
        }

        return CourierProductListResponse.from(
                recommended.stream().limit(limit).toList());
    }

    public CourierProductListResponse searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isBlank()) {
            return CourierProductListResponse.from(List.of());
        }
        List<CourierProduct> products = courierProductService.searchByName(keyword.trim());
        return CourierProductListResponse.from(products);
    }

    public List<CourierProductsByCategoryResponse> getProductsByCategory(int perCategoryLimit) {
        List<CourierProductCategory> categories =
                courierProductCategoryService.findAllOrderBySortOrder();
        List<CourierProductsByCategoryResponse> result = new ArrayList<>();

        for (CourierProductCategory cat : categories) {
            List<CourierProduct> products = courierProductService.findVisibleByCategoryLimited(
                    cat.getId(), perCategoryLimit);
            if (!products.isEmpty()) {
                result.add(new CourierProductsByCategoryResponse(
                        cat.getId(),
                        cat.getName(),
                        products.stream().map(CourierProductResponse::from).toList()));
            }
        }
        return result;
    }
}
