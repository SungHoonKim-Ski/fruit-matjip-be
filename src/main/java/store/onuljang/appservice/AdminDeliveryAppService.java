package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.exception.AdminValidateException;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.enums.DeliveryStatus;
import store.onuljang.service.DeliveryOrderService;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminDeliveryAppService {
    DeliveryOrderService deliveryOrderService;

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
            case CANCELED -> order.markCanceled();
            default -> throw new AdminValidateException("변경할 수 없는 상태입니다.");
        }
    }
}
