package store.onuljang.shared.feign.dto.reseponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoPayOrderResponse(
    String tid,
    String cid,
    String status,
    @JsonProperty("payment_action_details")
    List<PaymentActionDetail> paymentActionDetails
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentActionDetail(
        String aid,
        @JsonProperty("payment_action_type")
        String paymentActionType
    ) {}

    public boolean isSuccessPayment() {
        return "SUCCESS_PAYMENT".equals(status);
    }

    public boolean isTerminalFailure() {
        return "CANCEL_PAYMENT".equals(status)
            || "FAIL_PAYMENT".equals(status)
            || "QUIT_PAYMENT".equals(status);
    }

    public String getApproveAid() {
        if (paymentActionDetails == null) return null;
        return paymentActionDetails.stream()
            .filter(d -> "PAYMENT".equals(d.paymentActionType()))
            .map(PaymentActionDetail::aid)
            .findFirst()
            .orElse(null);
    }
}
