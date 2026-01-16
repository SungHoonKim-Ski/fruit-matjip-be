package store.onuljang.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.UniqueElements;
import store.onuljang.controller.response.ProductKeywordResponse;

import java.util.List;

public record AdminProductKeywordsRequest(
    @UniqueElements @Size(min = 1) @NotNull @Valid List<@Valid KeywordItem> keywords) {

    public record KeywordItem(
        @NotBlank(message = "키워드는 필수입니다")
        @Size(max = 5, message = "키워드는 5자 이하여야 합니다")
        String keyword,

        String keywordUrl
    ){

    }
}
