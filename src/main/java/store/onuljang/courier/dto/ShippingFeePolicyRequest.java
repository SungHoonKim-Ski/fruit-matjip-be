package store.onuljang.courier.dto;

import java.math.BigDecimal;

public record ShippingFeePolicyRequest(
        Integer minQuantity,
        Integer maxQuantity,
        BigDecimal fee,
        Integer sortOrder) {}
