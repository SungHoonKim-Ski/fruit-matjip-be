package store.onuljang.controller.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

public record AdminProductKeywordsRequest(
    @Size(min = 1, max = 10) @NotNull List<@NotBlank String> keywords
) {

}