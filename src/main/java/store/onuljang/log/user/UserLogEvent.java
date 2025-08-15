package store.onuljang.log.user;

import lombok.Builder;

@Builder
public record UserLogEvent(
    String userUid,
    String path,
    String method,
    Integer status,
    Long durationMs,
    String requestId)
{ }