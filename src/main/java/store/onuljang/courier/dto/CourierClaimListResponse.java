package store.onuljang.courier.dto;

import java.util.List;
import store.onuljang.courier.entity.CourierClaim;

public record CourierClaimListResponse(List<CourierClaimResponse> claims) {
    public static CourierClaimListResponse from(List<CourierClaim> claims) {
        return new CourierClaimListResponse(
                claims.stream().map(CourierClaimResponse::from).toList());
    }
}
