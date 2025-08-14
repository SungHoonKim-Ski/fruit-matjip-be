package store.onuljang.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PresignedUrlRequest(
    @NotBlank String fileName,
    @Pattern(regexp = "image/[-+.\\w]+") @NotBlank String contentType
) {}
