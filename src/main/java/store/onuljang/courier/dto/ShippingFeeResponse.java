package store.onuljang.courier.dto;

import java.math.BigDecimal;

public record ShippingFeeResponse(
        BigDecimal shippingFee,
        BigDecimal islandSurcharge,
        boolean isIsland,
        BigDecimal totalShippingFee) {

    public static ShippingFeeResponse from(ShippingFeeResult result) {
        return new ShippingFeeResponse(
                result.shippingFee(),
                result.islandSurcharge(),
                result.isIsland(),
                result.totalShippingFee());
    }
}
