package store.onuljang.courier.dto;

import java.util.List;

public record ShippingFeePolicyListResponse(List<ShippingFeePolicyResponse> policies) {

    public static ShippingFeePolicyListResponse from(
            List<store.onuljang.courier.entity.ShippingFeePolicy> policies) {
        return new ShippingFeePolicyListResponse(
                policies.stream().map(ShippingFeePolicyResponse::from).toList());
    }
}
