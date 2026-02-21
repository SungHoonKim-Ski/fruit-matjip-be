package store.onuljang.shared.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KakaoPayConfigDto {
    @Value("${KAKAOPAY.ENABLED:true}")
    boolean enabled;

    @Value("${KAKAOPAY.SECRET_KEY}")
    String secretKey;

    @Value("${KAKAOPAY.CID}")
    String cid;

    @Value("${KAKAOPAY.APPROVAL_URL}")
    String approvalUrl;

    @Value("${KAKAOPAY.CANCEL_URL}")
    String cancelUrl;

    @Value("${KAKAOPAY.FAIL_URL}")
    String failUrl;

    @Value("${KAKAOPAY.HOST:https://open-api.kakaopay.com}")
    String host;

    @Value("${KAKAOPAY.COURIER_APPROVAL_URL:}")
    String courierApprovalUrl;

    @Value("${KAKAOPAY.COURIER_CANCEL_URL:}")
    String courierCancelUrl;

    @Value("${KAKAOPAY.COURIER_FAIL_URL:}")
    String courierFailUrl;

    public KakaoPayRedirectUrls buildRedirectUrls(String displayCode) {
        return new KakaoPayRedirectUrls(
            approvalUrl + "?order_id=" + displayCode,
            cancelUrl + "?order_id=" + displayCode,
            failUrl + "?order_id=" + displayCode
        );
    }

    public KakaoPayRedirectUrls buildCourierRedirectUrls(String displayCode) {
        return new KakaoPayRedirectUrls(
            courierApprovalUrl + "?order_id=" + displayCode,
            courierCancelUrl + "?order_id=" + displayCode,
            courierFailUrl + "?order_id=" + displayCode
        );
    }

    public record KakaoPayRedirectUrls(String approvalUrl, String cancelUrl, String failUrl) {}
}
