package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.exception.AdminValidateException;
import store.onuljang.feign.dto.request.KakaoPayCancelRequest;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.enums.DeliveryStatus;
import store.onuljang.service.DeliveryOrderService;
import store.onuljang.service.DeliveryPaymentService;
import store.onuljang.service.KakaoPayService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminDeliveryAppService {
    DeliveryOrderService deliveryOrderService;
    DeliveryPaymentService deliveryPaymentService;
    KakaoPayService kakaoPayService;

    @Transactional
    public void accept(long orderId, int estimatedMinutes) {
        DeliveryOrder order = deliveryOrderService.findById(orderId);
        order.accept(estimatedMinutes);
    }

    @Transactional
    public void updateStatus(long orderId, DeliveryStatus nextStatus) {
        DeliveryOrder order = deliveryOrderService.findById(orderId);
        validateAdminTransition(order, nextStatus);
        applyStatus(order, nextStatus);
    }

    private void validateAdminTransition(DeliveryOrder order, DeliveryStatus nextStatus) {
        switch (nextStatus) {
            case OUT_FOR_DELIVERY -> {
                if (!order.canMarkOutForDelivery()) {
                    throw new AdminValidateException("배달 시작은 결제 완료 상태에서만 가능합니다.");
                }
            }
            case DELIVERED -> {
                if (!order.canMarkDelivered()) {
                    throw new AdminValidateException("배달 완료는 배달중 상태에서만 가능합니다.");
                }
            }
            case CANCELED -> {
                if (!order.canCancelByAdmin()) {
                    throw new AdminValidateException("배달 완료된 주문은 취소할 수 없습니다.");
                }
            }
            default -> throw new AdminValidateException("변경할 수 없는 상태입니다.");
        }
    }

    private void applyStatus(DeliveryOrder order, DeliveryStatus nextStatus) {
        switch (nextStatus) {
            case OUT_FOR_DELIVERY -> order.markOutForDelivery();
            case DELIVERED -> order.markDelivered();
            case CANCELED -> cancelOrder(order);
            default -> throw new AdminValidateException("변경할 수 없는 상태입니다.");
        }
    }

    private void cancelOrder(DeliveryOrder order) {
        if (order.isPaid() && order.getKakaoTid() != null) {
            try {
                int cancelAmount = deliveryPaymentService.getApprovedAmount(order);
                kakaoPayService.cancel(
                    new KakaoPayCancelRequest(null, order.getKakaoTid(), cancelAmount, 0));
            } catch (Exception e) {
                // PG사에서 이미 취소된 경우 등 — DB 상태는 반드시 취소 처리
                log.warn("카카오페이 취소 실패 (orderId={}): {}", order.getId(), e.getMessage());
            }
        }
        order.markCanceled();
        deliveryPaymentService.markCanceled(order);
    }

    @Transactional
    public long processAutoCompleteDelivery(LocalDateTime cutoff) {
        List<DeliveryOrder> orders = deliveryOrderService.findOutForDeliveryBefore(cutoff);
        orders.forEach(DeliveryOrder::markDelivered);
        return orders.size();
    }
}
