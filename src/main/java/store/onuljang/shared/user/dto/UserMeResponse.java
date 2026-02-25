package store.onuljang.shared.user.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import store.onuljang.shared.user.entity.Users;

@Builder
public record UserMeResponse(
    String nickname,
    boolean changeName,
    boolean restricted,
    LocalDate restrictedUntil,
    BigDecimal pointBalance
) {
    public static UserMeResponse from(Users user) {
        return UserMeResponse.builder()
            .nickname(user.getName())
            .changeName(user.getChangeName())
            .restricted(user.isRestricted())
            .restrictedUntil(user.getRestrictedUntil())
            .pointBalance(user.getPointBalance())
            .build();
    }
}
