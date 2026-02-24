package store.onuljang.courier.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourierProductUpdateRequest(
    String name,
    String productUrl,
    BigDecimal price,
    Integer weightGram,
    String description,
    Integer sortOrder,
    Boolean visible,
    Boolean soldOut,
    List<Long> categoryIds,
    Long shippingFeeTemplateId,
    BigDecimal combinedShippingFee,
    List<OptionGroupRequest> optionGroups
) {}
