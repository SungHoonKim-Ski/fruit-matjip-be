package store.onuljang.shared.user.event;

import lombok.Builder;
import store.onuljang.shared.entity.enums.MessageType;

@Builder
public record UserMessageEvent(
    String userUid,
    MessageType type
)
{

}
