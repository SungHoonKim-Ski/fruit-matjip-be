package store.onuljang.shared.user.dto;

import lombok.Builder;
import store.onuljang.shared.user.entity.UserMessageQueue;

@Builder
public record UserMessageResponse(
    long id,
    String title,
    String body
) {
    public static UserMessageResponse from(UserMessageQueue message) {
        return UserMessageResponse.builder()
            .id(message.getId())
            .title(message.getMessageTemplateTitle())
            .body(message.getMessageTemplateBody())
        .build();
    }
}
