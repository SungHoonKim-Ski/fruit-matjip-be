package store.onuljang.shared.user.dto;

import lombok.Builder;
import store.onuljang.shared.user.entity.Users;

import java.time.LocalDate;

@Builder
public record UserMeResponse(
    String nickname,
    boolean restricted,
    LocalDate restrictedUntil
) {
    public static UserMeResponse from(Users user) {
        return UserMeResponse.builder()
            .nickname(user.getName())
            .restricted(user.isRestricted())
            .restrictedUntil(user.getRestrictedUntil())
            .build();
    }
}
