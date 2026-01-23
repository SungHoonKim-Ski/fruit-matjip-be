package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.DeliveryPayment;

import java.util.Optional;

@Repository
public interface DeliveryPaymentRepository extends JpaRepository<DeliveryPayment, Long> {
    Optional<DeliveryPayment> findTopByDeliveryOrderOrderByIdDesc(DeliveryOrder deliveryOrder);
}
