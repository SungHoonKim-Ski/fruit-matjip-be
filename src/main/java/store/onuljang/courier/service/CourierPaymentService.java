package store.onuljang.courier.service;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierPayment;
import store.onuljang.courier.repository.CourierPaymentRepository;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierPaymentService {

    CourierPaymentRepository courierPaymentRepository;

    @Transactional
    public CourierPayment save(CourierPayment payment) {
        return courierPaymentRepository.save(payment);
    }

    @Transactional
    public void markApproved(CourierOrder order, String aid) {
        findLatestByOrder(order).ifPresent(payment -> payment.markApproved(aid));
    }

    @Transactional
    public void markCanceled(CourierOrder order) {
        findLatestByOrder(order).ifPresent(CourierPayment::markCanceled);
    }

    @Transactional
    public void markFailed(CourierOrder order) {
        findLatestByOrder(order).ifPresent(CourierPayment::markFailed);
    }

    public Optional<CourierPayment> findByCourierOrder(CourierOrder order) {
        return findLatestByOrder(order);
    }

    private Optional<CourierPayment> findLatestByOrder(CourierOrder order) {
        return courierPaymentRepository.findTopByCourierOrderOrderByIdDesc(order);
    }
}
