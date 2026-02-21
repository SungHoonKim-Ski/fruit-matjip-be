package store.onuljang.shared.feign.dto.reseponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoPayApproveResponse(
    String aid,
    String tid,
    String cid,
    String partnerOrderId,
    String partnerUserId,
    String paymentMethodType,
    String itemName,
    String approvedAt
) {}
