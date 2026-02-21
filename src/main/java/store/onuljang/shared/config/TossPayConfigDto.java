package store.onuljang.shared.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class TossPayConfigDto {

    @Value("${TOSSPAY.ENABLED:false}")
    boolean enabled;

    @Value("${TOSSPAY.SECRET_KEY:}")
    String secretKey;

    @Value("${TOSSPAY.HOST:https://api.tosspayments.com}")
    String host;
}
