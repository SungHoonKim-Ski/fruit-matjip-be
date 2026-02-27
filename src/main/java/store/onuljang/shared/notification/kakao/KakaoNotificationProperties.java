package store.onuljang.shared.notification.kakao;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KakaoNotificationProperties {

    @Value("${KAKAO_NOTIFICATION.ENABLED:false}")
    boolean enabled;

    @Value("${KAKAO_NOTIFICATION.SITE_URL:https://fruit-matjip.store}")
    String siteUrl;

    @Value("${cloud.aws.sqs.kakao-notification-queue-url:}")
    String queueUrl;
}
