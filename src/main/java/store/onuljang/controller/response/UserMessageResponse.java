package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.UserMessageQueue;


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