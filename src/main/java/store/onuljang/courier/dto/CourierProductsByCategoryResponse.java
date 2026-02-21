package store.onuljang.courier.dto;

import java.util.List;

public record CourierProductsByCategoryResponse(
    Long categoryId,
    String categoryName,
    List<CourierProductResponse> products
) {}
