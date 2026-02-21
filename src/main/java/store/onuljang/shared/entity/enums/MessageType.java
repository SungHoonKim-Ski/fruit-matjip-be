package store.onuljang.shared.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    USER_NO_SHOW(50),
    NOTICE(100);

    private final int priority;
}
