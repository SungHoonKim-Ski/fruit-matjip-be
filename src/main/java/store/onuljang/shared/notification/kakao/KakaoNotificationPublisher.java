package store.onuljang.shared.notification.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import store.onuljang.shared.notification.kakao.dto.KakaoNotificationMessage;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KakaoNotificationPublisher {

    KakaoNotificationProperties properties;
    SqsClient sqsClient;
    ObjectMapper objectMapper;

    public void publish(String tplCode, String receiverPhone, String receiverName,
            Map<String, String> variables, String buttonUrl) {

        if (!properties.isEnabled()) {
            log.debug("[KakaoNotification] disabled, skip: tplCode={}", tplCode);
            return;
        }

        String queueUrl = properties.getQueueUrl();
        if (queueUrl == null || queueUrl.isBlank()) {
            log.warn("[KakaoNotification] queue URL not configured, skip: tplCode={}", tplCode);
            return;
        }

        if (tplCode == null || tplCode.isBlank()) {
            log.debug("[KakaoNotification] no tplCode, skip");
            return;
        }

        KakaoNotificationMessage message = new KakaoNotificationMessage(
            tplCode,
            receiverPhone,
            receiverName != null ? receiverName : "",
            variables,
            buttonUrl
        );

        try {
            String body = objectMapper.writeValueAsString(message);
            sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .build());
            log.info("[KakaoNotification] queued: tplCode={}, receiver={}",
                tplCode, maskPhone(receiverPhone));
        } catch (JsonProcessingException e) {
            log.error("[KakaoNotification] serialize failed: tplCode={}, error={}",
                tplCode, e.getMessage(), e);
        } catch (Exception e) {
            log.error("[KakaoNotification] SQS send failed: tplCode={}, error={}",
                tplCode, e.getMessage(), e);
        }
    }

    public String getSiteUrl() {
        return properties.getSiteUrl();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "***";
        }
        return phone.substring(0, phone.length() - 4) + "****";
    }
}
