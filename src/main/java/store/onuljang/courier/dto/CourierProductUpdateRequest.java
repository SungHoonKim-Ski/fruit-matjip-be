package store.onuljang.courier.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourierProductUpdateRequest(
    String name,
    String productUrl,
    BigDecimal price,
    Integer stock,
    Integer weightGram,
    String description,
    Integer sortOrder,
    Boolean visible,
    List<Long> categoryIds,
    List<String> detailImageUrls
) {}
