package store.onuljang.config;

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

    public KakaoPayRedirectUrls buildRedirectUrls(String displayCode) {
        return new KakaoPayRedirectUrls(
            approvalUrl + "?order_id=" + displayCode,
            cancelUrl + "?order_id=" + displayCode,
            failUrl + "?order_id=" + displayCode
        );
    }

    public record KakaoPayRedirectUrls(String approvalUrl, String cancelUrl, String failUrl) {}
}
