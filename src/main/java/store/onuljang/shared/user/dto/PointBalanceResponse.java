package store.onuljang.shared.user.dto;

import java.math.BigDecimal;
import java.util.List;

public record PointBalanceResponse(
    BigDecimal balance,
    List<PointTransactionResponse> recentHistory
) {}
