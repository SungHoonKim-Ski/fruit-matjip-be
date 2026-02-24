package store.onuljang.shared.user.dto;

import java.math.BigDecimal;
import store.onuljang.shared.user.entity.Users;

public record AdminPointUserResponse(
    String uid,
    String name,
    BigDecimal pointBalance
) {
    public static AdminPointUserResponse from(Users user) {
        return new AdminPointUserResponse(user.getUid(), user.getName(), user.getPointBalance());
    }
}
