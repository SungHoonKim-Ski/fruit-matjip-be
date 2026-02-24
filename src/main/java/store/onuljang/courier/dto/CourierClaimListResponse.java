package store.onuljang.courier.dto;

import java.util.List;
import org.springframework.data.domain.Page;
import store.onuljang.courier.entity.CourierClaim;

public record CourierClaimListResponse(
        List<CourierClaimResponse> claims,
        int totalPages,
        long totalElements,
        int currentPage) {

    public static CourierClaimListResponse from(Page<CourierClaim> page) {
        return new CourierClaimListResponse(
                page.getContent().stream().map(CourierClaimResponse::from).toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber());
    }
}
