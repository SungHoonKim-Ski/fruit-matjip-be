package store.onuljang.event.delivery;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.service.AdminDeliverySseService;
import store.onuljang.service.DeliveryOrderService;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryPaidEventListener {
    DeliveryOrderService deliveryOrderService;
    AdminDeliverySseService adminDeliverySseService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DeliveryPaidEvent event) {
        DeliveryOrder order = deliveryOrderService.findById(event.orderId());
        adminDeliverySseService.notifyPaid(order);
    }
}
