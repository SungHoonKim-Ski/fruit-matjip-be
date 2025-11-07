package store.onuljang.event.user_message;

import lombok.Builder;
import store.onuljang.repository.entity.enums.MessageType;

@Builder
public record UserMessageEvent(
    String userUid,
    MessageType type
)
{

}