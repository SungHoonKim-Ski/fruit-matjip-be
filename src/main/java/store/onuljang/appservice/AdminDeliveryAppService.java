package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.exception.UserValidateException;
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
        DeliveryOrder order = deliveryOrderService.findByIdWithLock(orderId);
        DeliveryStatus current = order.getStatus();

        if (nextStatus == DeliveryStatus.OUT_FOR_DELIVERY) {
            if (current != DeliveryStatus.PAID) {
                throw new UserValidateException("배달 시작은 결제 완료 상태에서만 가능합니다.");
            }
            order.markOutForDelivery();
            return;
        }

        if (nextStatus == DeliveryStatus.DELIVERED) {
            if (current != DeliveryStatus.OUT_FOR_DELIVERY) {
                throw new UserValidateException("배달 완료는 배달중 상태에서만 가능합니다.");
            }
            order.markDelivered();
            return;
        }

        if (nextStatus == DeliveryStatus.CANCELED) {
            if (current == DeliveryStatus.DELIVERED) {
                throw new UserValidateException("배달 완료된 주문은 취소할 수 없습니다.");
            }
            order.markCanceled();
            return;
        }

        throw new UserValidateException("변경할 수 없는 상태입니다.");
    }
}
