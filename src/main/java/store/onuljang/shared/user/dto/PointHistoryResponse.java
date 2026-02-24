package store.onuljang.shared.user.dto;

import java.util.List;
import org.springframework.data.domain.Page;
import store.onuljang.shared.user.entity.UserPointTransaction;

public record PointHistoryResponse(
    List<PointTransactionResponse> transactions,
    int totalPages,
    long totalElements,
    int currentPage
) {
    public static PointHistoryResponse from(Page<UserPointTransaction> page) {
        return new PointHistoryResponse(
            page.getContent().stream().map(PointTransactionResponse::from).toList(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.getNumber()
        );
    }
}
