package store.onuljang.shared.feign.dto.request;

public record KakaoPayCancelRequest(
    String cid,
    String tid,
    int cancelAmount,
    int cancelTaxFreeAmount
) {}
