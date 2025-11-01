package store.onuljang.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PresignedUrlBatchRequest(
    @Size(min = 1, max = 10) @NotNull List<@NotBlank String> fileNames,
    @Pattern(regexp = "image/[-+.\\w]+") @NotBlank String contentType
) {}