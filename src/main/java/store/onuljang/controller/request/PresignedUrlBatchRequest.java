package store.onuljang.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PresignedUrlBatchRequest(
    @NotNull Long adminId,
    @NotNull Long productId,
    @Size(min = 1, max = 10) List<@NotBlank String> filenames,
    @Pattern(regexp = "image/[-+.\\w]+") @NotBlank String contentType
) {}