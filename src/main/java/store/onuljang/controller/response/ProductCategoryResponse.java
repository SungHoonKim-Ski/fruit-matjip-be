package store.onuljang.controller.response;

import store.onuljang.repository.entity.ProductCategory;

import java.util.List;

public record ProductCategoryResponse(List<ProductCategoryItem> response) {

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
