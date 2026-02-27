package store.onuljang.worker.listener.kakao;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import store.onuljang.shared.notification.kakao.dto.KakaoNotificationMessage;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KakaoNotificationMessageProcessor {

    private static final String ALIGO_API_URL = "https://kakaoapi.aligo.in/akv10/alimtalk/send/";

    @NonNull SqsClient sqsClient;
    @NonNull ObjectMapper objectMapper;
    @NonNull AligoProperties aligoProperties;

    private static final Map<String, Template> TEMPLATES = Map.of(
        "UF_7406", new Template(
            "[과일맛집 1995] 배송이 시작되었어요.\n\n▶ 주문번호: #{주문번호}\n▶ 주문상품: #{주문상품}\n▶ 택배사: #{택배사}\n▶ 송장번호: #{송장번호}\n\n문의: 주문내역 > 배송상품 > 문의하기",
            "배송시작", "배송조회"),
        "UF_7407", new Template(
            "[과일맛집 1995] 상품이 배송 중이에요.\n\n▶ 주문번호: #{주문번호}\n▶ 주문상품: #{주문상품}\n▶ 택배사: #{택배사}\n▶ 송장번호: #{송장번호}\n\n문의: 주문내역 > 배송상품 > 문의하기",
            "배송중", "배송조회"),
        "UF_7408", new Template(
            "[과일맛집 1995] 배송이 완료되었어요.\n\n▶ 주문번호: #{주문번호}\n▶ 주문상품: #{주문상품}\n▶ 택배사: #{택배사}\n▶ 송장번호: #{송장번호}\n\n문의: 주문내역 > 배송상품 > 문의하기",
            "배송완료", "배송조회"),
        "UF_7409", new Template(
            "[과일맛집 1995] 배송이 취소되었어요.\n\n▶ 주문번호: #{주문번호}\n▶ 주문상품: #{주문상품}\n\n문의: 주문내역 > 배송상품 > 문의하기",
            "배송취소", "배송조회"),
        "UF_7410", new Template(
            "[과일맛집 1995] 문의하신 내용에 답변이 등록되었어요.\n\n▶ 주문번호: #{주문번호}\n▶ 주문상품: #{주문상품}\n\n문의: 주문내역 > 배송상품 > 문의하기",
            "문의답변", "답변 조회")
    );

    private record Template(String message, String subject, String buttonName) {}

    @Transactional
    public void process(Message message, String queueUrl) {
        try {
            KakaoNotificationMessage notification = objectMapper.readValue(
                message.body(), KakaoNotificationMessage.class);

            String tplCode = notification.tplCode();
            Template template = TEMPLATES.get(tplCode);
            if (template == null) {
                log.error("[KakaoNotificationMessageProcessor] unknown tplCode={}, deleting message", tplCode);
                deleteMessage(queueUrl, message);
                return;
            }

            String builtMessage = buildMessage(template.message(), notification.variables());
            String buttonJson = buildButtonJson(template.buttonName(), notification.buttonUrl());

            boolean success = callAligoApi(
                tplCode,
                notification.receiverPhone(),
                notification.receiverName(),
                template.subject(),
                builtMessage,
                buttonJson
            );

            if (success) {
                deleteMessage(queueUrl, message);
                log.info("[KakaoNotificationMessageProcessor] sent: tplCode={}, receiver={}",
                    tplCode, maskPhone(notification.receiverPhone()));
            } else {
                log.warn("[KakaoNotificationMessageProcessor] Aligo API failed (transient), will retry: tplCode={}, receiver={}",
                    tplCode, maskPhone(notification.receiverPhone()));
            }
        } catch (Exception e) {
            log.error("[KakaoNotificationMessageProcessor] failed to process message: messageId={}, error={}",
                message.messageId(), e.getMessage(), e);
        }
    }

    private String buildMessage(String template, Map<String, String> variables) {
        String result = template;
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                result = result.replace("#{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return result;
    }

    private String buildButtonJson(String buttonName, String buttonUrl) {
        String escapedUrl = buttonUrl != null ? buttonUrl.replace("\"", "\\\"") : "";
        String escapedName = buttonName != null ? buttonName.replace("\"", "\\\"") : "";
        return "[{\"name\":\"채널추가\",\"linkType\":\"AC\",\"linkTypeName\":\"채널추가\"},"
            + "{\"name\":\"" + escapedName + "\",\"linkType\":\"WL\",\"linkTypeName\":\"웹링크\","
            + "\"linkMo\":\"" + escapedUrl + "\",\"linkPc\":\"" + escapedUrl + "\"}]";
    }

    private boolean callAligoApi(String tplCode, String receiverPhone, String receiverName,
            String subject, String messageText, String buttonJson) {
        try {
            StringJoiner params = new StringJoiner("&");
            params.add("apikey=" + encode(aligoProperties.getApiKey()));
            params.add("userid=" + encode(aligoProperties.getUserId()));
            params.add("senderkey=" + encode(aligoProperties.getSenderKey()));
            params.add("sender=" + encode(aligoProperties.getSenderPhone()));
            params.add("tpl_code=" + encode(tplCode));
            params.add("receiver_1=" + encode(receiverPhone));
            params.add("recvname_1=" + encode(receiverName != null ? receiverName : ""));
            params.add("subject_1=" + encode(subject));
            params.add("message_1=" + encode(messageText));
            params.add("button_1=" + encode(buttonJson));

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ALIGO_API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(params.toString()))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            Object codeObj = result.get("code");
            int code = codeObj instanceof Number ? ((Number) codeObj).intValue() : Integer.parseInt(codeObj.toString());

            if (code == 0) {
                return true;
            }

            String errorMessage = result.getOrDefault("message", "unknown").toString();
            log.warn("[KakaoNotificationMessageProcessor] Aligo API error: code={}, message={}", code, errorMessage);

            // Non-retryable errors: delete message to prevent infinite retry
            if (code == -99 || code == -101 || code == -201) {
                log.error("[KakaoNotificationMessageProcessor] non-retryable error (code={}), deleting message", code);
                return true; // caller will delete
            }

            return false;
        } catch (Exception e) {
            log.error("[KakaoNotificationMessageProcessor] HTTP call failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private void deleteMessage(String queueUrl, Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
            .queueUrl(queueUrl)
            .receiptHandle(message.receiptHandle())
            .build());
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "***";
        }
        return phone.substring(0, phone.length() - 4) + "****";
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
