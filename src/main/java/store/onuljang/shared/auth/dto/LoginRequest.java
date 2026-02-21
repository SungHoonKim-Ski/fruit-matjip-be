package store.onuljang.shared.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank
    String redirectUri,
    @NotBlank
    String code
) {

}
