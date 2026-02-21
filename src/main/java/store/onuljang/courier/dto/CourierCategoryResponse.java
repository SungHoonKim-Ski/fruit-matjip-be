package store.onuljang.courier.dto;

import java.util.List;
import store.onuljang.courier.entity.CourierProductCategory;

public record CourierCategoryResponse(List<CourierCategoryItem> response) {
    public record CourierCategoryItem(
            Long id, String name, String imageUrl, Integer sortOrder) {}

    public static CourierCategoryResponse of(List<CourierProductCategory> categories) {
        return new CourierCategoryResponse(
                categories.stream()
                        .map(
                                c ->
                                        new CourierCategoryItem(
                                                c.getId(),
                                                c.getName(),
                                                c.getImageUrl(),
                                                c.getSortOrder()))
                        .toList());
    }
}
