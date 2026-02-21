package store.onuljang.courier.dto;

import java.math.BigDecimal;
import lombok.Builder;
import store.onuljang.courier.entity.ShippingFeeTemplate;

@Builder
public record ShippingFeeTemplateResponse(
        Long id,
        String name,
        BigDecimal baseFee,
        BigDecimal perQuantityFee,
        BigDecimal freeShippingMinAmount,
        boolean active,
        int sortOrder) {

    public static ShippingFeeTemplateResponse from(ShippingFeeTemplate t) {
        return ShippingFeeTemplateResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .baseFee(t.getBaseFee())
                .perQuantityFee(t.getPerQuantityFee())
                .freeShippingMinAmount(t.getFreeShippingMinAmount())
                .active(t.getActive())
                .sortOrder(t.getSortOrder())
                .build();
    }
}
