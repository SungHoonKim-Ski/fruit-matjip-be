package store.onuljang.courier.dto;

import java.math.BigDecimal;
import store.onuljang.courier.entity.ShippingFeePolicy;

public record ShippingFeePolicyResponse(
        Long id,
        Integer minQuantity,
        Integer maxQuantity,
        BigDecimal fee,
        Integer sortOrder,
        Boolean active) {

    public static ShippingFeePolicyResponse from(ShippingFeePolicy policy) {
        return new ShippingFeePolicyResponse(
                policy.getId(),
                policy.getMinQuantity(),
                policy.getMaxQuantity(),
                policy.getFee(),
                policy.getSortOrder(),
                policy.getActive());
    }
}
