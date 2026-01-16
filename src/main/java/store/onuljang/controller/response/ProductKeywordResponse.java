package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.ProductKeyword;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public record ProductKeywordResponse(List<KeywordItem> response) {
    public static ProductKeywordResponse of(List<ProductKeyword> keywords) {
        return ProductKeywordResponse.builder()
            .response(
                keywords.stream().map(entity ->
                    new KeywordItem(entity.getName(), entity.getKeywordUrl())
                ).toList()).build();
    }
    public record KeywordItem(
        String keyword,
        String keywordUrl
    ) { }
}
