package store.onuljang.controller.request;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;


public record AdminProductKeywordsRequest(
    @UniqueElements @Size(min = 1, max = 7) @NotNull List<@NotBlank String> keywords
) {

}