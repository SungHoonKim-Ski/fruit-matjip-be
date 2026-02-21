package store.onuljang.shared.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class NaverPayConfigDto {

    @Value("${NAVERPAY.ENABLED:false}")
    boolean enabled;

    @Value("${NAVERPAY.CLIENT_ID:}")
    String clientId;

    @Value("${NAVERPAY.CLIENT_SECRET:}")
    String clientSecret;

    @Value("${NAVERPAY.CHAIN_ID:}")
    String chainId;

    @Value("${NAVERPAY.HOST:https://dev.apis.naver.com}")
    String host;
}
