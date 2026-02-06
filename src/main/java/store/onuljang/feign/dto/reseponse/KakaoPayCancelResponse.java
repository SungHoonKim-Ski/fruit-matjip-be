package store.onuljang.feign.dto.reseponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoPayCancelResponse(
    String aid,
    String tid,
    String status,
    String approvedAt,
    String canceledAt
) {}
