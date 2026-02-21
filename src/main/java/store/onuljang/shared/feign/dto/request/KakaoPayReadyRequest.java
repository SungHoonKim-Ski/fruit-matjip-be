package store.onuljang.shared.feign.dto.request;

public record KakaoPayReadyRequest(
    String cid,
    String partnerOrderId,
    String partnerUserId,
    String itemName,
    int quantity,
    int totalAmount,
    int taxFreeAmount,
    String approvalUrl,
    String cancelUrl,
    String failUrl
) {}
