package store.onuljang.feign.dto.request;

public record KakaoPayApproveRequest(
    String tid,
    String partnerOrderId,
    String partnerUserId,
    String pgToken
) {}
