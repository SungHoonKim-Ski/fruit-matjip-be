package store.onuljang.controller.response;

import lombok.Builder;

@Builder
public record LoginResponse(
    String name,
    String access,
    String refresh
) {

}
