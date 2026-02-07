package store.onuljang.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.KakaoPayConfigDto;
import store.onuljang.feign.dto.reseponse.KakaoPayOrderResponse;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.service.DeliveryOrderService;
import store.onuljang.service.DeliveryPaymentService;
import store.onuljang.service.KakaoPayService;
import store.onuljang.util.TimeUtil;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeliveryPaymentReconciliationScheduler {
    DeliveryOrderService deliveryOrderService;
    DeliveryPaymentService deliveryPaymentService;
    KakaoPayService kakaoPayService;
    KakaoPayConfigDto kakaoPayConfigDto;

    private static final int RECONCILE_AFTER_MINUTES = 2;

    @Scheduled(fixedRate = 180_000)
    @Transactional
    public void reconcilePendingPayments() {
        if (!kakaoPayConfigDto.isEnabled()) return;

        LocalDateTime cutoff = TimeUtil.nowDateTime().minusMinutes(RECONCILE_AFTER_MINUTES);
        List<DeliveryOrder> pending = deliveryOrderService.findPendingPaymentsWithTid(cutoff);

        int reconciled = 0;
        for (DeliveryOrder order : pending) {
            try {
                reconciled += reconcileOrder(order) ? 1 : 0;
            } catch (Exception e) {
                log.warn(
                        "[Reconciliation] 조회 실패 (orderId={}): {}",
                        order.getId(),
                        e.getMessage());
            }
        }

        if (reconciled > 0) {
            log.info(
                    "[Reconciliation] reconciled {} / {} pending payments",
                    reconciled,
                    pending.size());
        }
    }

    private boolean reconcileOrder(DeliveryOrder order) {
        KakaoPayOrderResponse pgOrder = kakaoPayService.order(order.getKakaoTid());

        if (pgOrder.isSuccessPayment()) {
            String aid = pgOrder.getApproveAid();
            deliveryOrderService.completePaid(order.getId(), aid);
            log.info(
                    "[Reconciliation] 결제 확인 → PAID (orderId={}, aid={})",
                    order.getId(),
                    aid);
            return true;
        }

        if (pgOrder.isTerminalFailure()) {
            order.markFailed();
            deliveryPaymentService.markFailed(order);
            log.info(
                    "[Reconciliation] PG 실패/취소 → FAILED (orderId={}, pgStatus={})",
                    order.getId(),
                    pgOrder.status());
            return true;
        }

        return false;
    }
}
