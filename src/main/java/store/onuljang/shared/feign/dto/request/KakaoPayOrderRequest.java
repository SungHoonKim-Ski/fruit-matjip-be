package store.onuljang.shared.feign.dto.request;

public record KakaoPayOrderRequest(
    String cid,
    String tid
) {}
