package store.onuljang.controller.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank
    String redirectUri,
    @NotBlank
    String code
) {

}
