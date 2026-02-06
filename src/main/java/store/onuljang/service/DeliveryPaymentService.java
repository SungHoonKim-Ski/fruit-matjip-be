package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.DeliveryPaymentRepository;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.DeliveryPayment;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryPaymentService {
    DeliveryPaymentRepository deliveryPaymentRepository;

    @Transactional
    public DeliveryPayment save(DeliveryPayment payment) {
        return deliveryPaymentRepository.save(payment);
    }

    @Transactional
    public void markApproved(DeliveryOrder order, String aid) {
        this.findLatestByOrder(order).ifPresent(payment -> payment.markApproved(aid));
    }

    @Transactional
    public void markCanceled(DeliveryOrder order) {
        findLatestByOrder(order).ifPresent(DeliveryPayment::markCanceled);
    }

    @Transactional
    public void markFailed(DeliveryOrder order) {
        findLatestByOrder(order).ifPresent(DeliveryPayment::markFailed);
    }

    public int getApprovedAmount(DeliveryOrder order) {
        return findLatestByOrder(order)
            .map(payment -> payment.getAmount().intValue())
            .orElse(0);
    }

    private Optional<DeliveryPayment> findLatestByOrder(DeliveryOrder order) {
        return deliveryPaymentRepository.findTopByDeliveryOrderOrderByIdDesc(order);
    }
}
