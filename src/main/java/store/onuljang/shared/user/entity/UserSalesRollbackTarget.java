package store.onuljang.shared.user.entity;

import java.math.BigDecimal;

public record UserSalesRollbackTarget(
    String userUid,
    int totalQuantity,
    BigDecimal totalAmount,
    int rows
) {

}
