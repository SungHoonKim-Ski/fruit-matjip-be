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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryPaymentService {
    DeliveryPaymentRepository deliveryPaymentRepository;

    @Transactional
    public DeliveryPayment save(DeliveryPayment payment) {
        return deliveryPaymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Optional<DeliveryPayment> findLatestByOrder(DeliveryOrder order) {
        return deliveryPaymentRepository.findTopByDeliveryOrderOrderByIdDesc(order);
    }
}
