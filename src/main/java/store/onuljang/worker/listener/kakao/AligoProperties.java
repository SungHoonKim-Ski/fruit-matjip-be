package store.onuljang.worker.listener.kakao;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AligoProperties {

    @Value("${ALIGO.API_KEY:}")
    String apiKey;

    @Value("${ALIGO.USER_ID:}")
    String userId;

    @Value("${ALIGO.SENDER_KEY:}")
    String senderKey;

    @Value("${ALIGO.SENDER_PHONE:}")
    String senderPhone;
}
