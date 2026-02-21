package store.onuljang.shared.auth.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
    String name,
    String access,
    boolean changeName
) {

}
