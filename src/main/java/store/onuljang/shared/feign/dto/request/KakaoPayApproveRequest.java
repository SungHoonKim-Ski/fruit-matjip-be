package store.onuljang.shared.feign.dto.request;

public record KakaoPayApproveRequest(
    String cid,
    String tid,
    String partnerOrderId,
    String partnerUserId,
    String pgToken
) {}
