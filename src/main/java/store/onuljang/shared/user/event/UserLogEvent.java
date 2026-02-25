package store.onuljang.shared.user.event;

import lombok.Builder;

@Builder
public record UserLogEvent(
    String userUid,
    String path,
    String method,
    Integer status,
    Long durationMs,
    String requestId,
    String clientIp)
{ }
