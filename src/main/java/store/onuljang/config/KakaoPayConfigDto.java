package store.onuljang.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KakaoPayConfigDto {
    @Value("${KAKAOPAY.ENABLED:true}")
    boolean enabled;

    @Value("${KAKAOPAY.ADMIN_KEY}")
    String adminKey;

    @Value("${KAKAOPAY.CID}")
    String cid;

    @Value("${KAKAOPAY.APPROVAL_URL}")
    String approvalUrl;

    @Value("${KAKAOPAY.CANCEL_URL}")
    String cancelUrl;

    @Value("${KAKAOPAY.FAIL_URL}")
    String failUrl;

    @Value("${KAKAOPAY.HOST:https://kapi.kakao.com}")
    String host;

    final String contentType = "application/x-www-form-urlencoded;charset=utf-8";
}
