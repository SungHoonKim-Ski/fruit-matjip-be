package store.onuljang.feign.dto.request;

public record KakaoPayReadyRequest(
    String partnerOrderId,
    String partnerUserId,
    String itemName,
    int quantity,
    int totalAmount,
    String approvalUrl,
    String cancelUrl,
    String failUrl
) {}
