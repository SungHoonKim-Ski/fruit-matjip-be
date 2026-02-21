package store.onuljang.courier.dto;

import java.util.List;
import store.onuljang.courier.entity.ShippingFeeTemplate;

public record ShippingFeeTemplateListResponse(List<ShippingFeeTemplateResponse> templates) {
    public static ShippingFeeTemplateListResponse from(List<ShippingFeeTemplate> templates) {
        return new ShippingFeeTemplateListResponse(
                templates.stream().map(ShippingFeeTemplateResponse::from).toList());
    }
}
