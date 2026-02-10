package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.Users;

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
