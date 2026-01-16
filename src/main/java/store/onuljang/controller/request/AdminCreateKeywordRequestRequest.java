package store.onuljang.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import store.onuljang.repository.entity.ProductKeyword;

public record AdminCreateKeywordRequestRequest(
    @NotBlank(message = "키워드는 필수입니다")
    @Size(max = 5, message = "키워드는 5자 이하여야 합니다")
    String keyword,

    String keywordUrl
) {
    public static ProductKeyword toEntity(AdminCreateKeywordRequestRequest request) {
        return ProductKeyword.builder()
            .name(request.keyword())
            .keywordUrl(request.keywordUrl)
            .build();
    }
}
