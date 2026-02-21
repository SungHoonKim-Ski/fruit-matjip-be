package store.onuljang.shop.delivery.scheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shop.delivery.service.DeliveryOrderService;
import store.onuljang.shop.delivery.service.DeliveryPaymentService;
import store.onuljang.shared.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeliveryPaymentExpireScheduler {
    DeliveryOrderService deliveryOrderService;
    DeliveryPaymentService deliveryPaymentService;

    private static final int EXPIRE_MINUTES = 5;

    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void expirePendingPayments() {
        LocalDateTime cutoff = TimeUtil.nowDateTime().minusMinutes(EXPIRE_MINUTES);
        List<DeliveryOrder> expired = deliveryOrderService.findExpiredPendingPayments(cutoff);

        for (DeliveryOrder order : expired) {
            order.markFailed();
            deliveryPaymentService.markFailed(order);
        }

        if (!expired.isEmpty()) {
            log.info("[DeliveryPaymentExpireScheduler] expired {} pending payments", expired.size());
        }
    }
}
