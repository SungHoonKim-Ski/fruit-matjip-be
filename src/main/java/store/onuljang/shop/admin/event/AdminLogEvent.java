package store.onuljang.shop.admin.event;

import lombok.Builder;

@Builder
public record AdminLogEvent(
    Long adminId,
    String path,
    String method,
    Integer status,
    Long durationMs,
    String requestId)
{ }
