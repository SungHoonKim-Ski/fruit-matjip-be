package store.onuljang.controller.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import store.onuljang.repository.entity.ProductCategory;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ProductCategoryResponse(List<ProductCategoryItem> response) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ProductCategoryItem(Long id, String name, String imageUrl, Integer sortOrder, List<Long> productIds) {
        public static ProductCategoryItem from(ProductCategory category) {
            return new ProductCategoryItem(category.getId(), category.getName(), category.getImageUrl(),
                    category.getSortOrder(), category.getProducts().stream().map(p -> p.getId()).toList());
        }
    }

    public static ProductCategoryResponse of(List<ProductCategory> categories) {
        return new ProductCategoryResponse(categories.stream().map(ProductCategoryItem::from).toList());
    }
}
