package store.onuljang.courier.event;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.notification.kakao.KakaoNotificationPublisher;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourierStatusChangedEventListener {

    KakaoNotificationPublisher kakaoNotificationPublisher;

    static final Map<String, String> COURIER_COMPANY_LABELS = Map.of(
        "LOGEN", "로젠택배",
        "HANJIN", "한진택배",
        "CJ", "CJ대한통운",
        "LOTTE", "롯데택배",
        "EPOST", "우체국택배"
    );

    @NonFinal
    @Value("${ALIGO.TPL_CODE_ORDER_COMPLETED:UF_7406}")
    String tplCodeOrderCompleted;

    @NonFinal
    @Value("${ALIGO.TPL_CODE_IN_TRANSIT:UF_7407}")
    String tplCodeInTransit;

    @NonFinal
    @Value("${ALIGO.TPL_CODE_DELIVERED:UF_7408}")
    String tplCodeDelivered;

    @NonFinal
    @Value("${ALIGO.TPL_CODE_CANCELED:UF_7409}")
    String tplCodeCanceled;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void handle(CourierStatusChangedEvent event) {
        String tplCode = resolveTplCode(event.newStatus());
        if (tplCode == null || tplCode.isBlank()) {
            return;
        }

        String courierLabel = COURIER_COMPANY_LABELS.getOrDefault(
            event.courierCompanyName(), event.courierCompanyName());
        String buttonUrl = kakaoNotificationPublisher.getSiteUrl()
            + "/shop/orders/" + event.displayCode();

        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("주문번호", event.displayCode());
        variables.put("주문상품", nullSafe(event.productSummary()));

        if (event.newStatus() != CourierOrderStatus.CANCELED) {
            variables.put("택배사", nullSafe(courierLabel));
            variables.put("송장번호", nullSafe(event.waybillNumber()));
        }

        kakaoNotificationPublisher.publish(
            tplCode,
            event.receiverPhone(),
            event.receiverName(),
            variables,
            buttonUrl
        );
    }

    private String resolveTplCode(CourierOrderStatus status) {
        return switch (status) {
            case ORDER_COMPLETED -> tplCodeOrderCompleted;
            case IN_TRANSIT -> tplCodeInTransit;
            case DELIVERED -> tplCodeDelivered;
            case CANCELED -> tplCodeCanceled;
            default -> null;
        };
    }

    private String nullSafe(String value) {
        return value != null ? value : "-";
    }
}
