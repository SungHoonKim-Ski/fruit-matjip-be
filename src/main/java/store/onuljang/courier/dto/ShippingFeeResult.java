package store.onuljang.courier.dto;

import java.math.BigDecimal;

public record ShippingFeeResult(
        BigDecimal shippingFee, BigDecimal islandSurcharge, boolean isIsland, BigDecimal totalShippingFee) {}
