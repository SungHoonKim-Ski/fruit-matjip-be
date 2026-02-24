package store.onuljang.courier.dto;

import java.math.BigDecimal;

public record ShippingFeeItemInput(
        Long productId,
        int quantity,
        BigDecimal itemAmount,
        Long templateId,
        BigDecimal combinedShippingFee) {}
