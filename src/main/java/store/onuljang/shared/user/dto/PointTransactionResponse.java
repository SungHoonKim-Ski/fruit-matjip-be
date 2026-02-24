package store.onuljang.shared.user.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import store.onuljang.shared.entity.enums.UserPointTransactionType;
import store.onuljang.shared.user.entity.UserPointTransaction;

public record PointTransactionResponse(
    Long id,
    UserPointTransactionType type,
    BigDecimal amount,
    BigDecimal balanceAfter,
    String description,
    LocalDateTime createdAt
) {
    public static PointTransactionResponse from(UserPointTransaction tx) {
        return new PointTransactionResponse(
            tx.getId(),
            tx.getType(),
            tx.getAmount(),
            tx.getBalanceAfter(),
            tx.getDescription(),
            tx.getCreatedAt()
        );
    }
}
