package store.onuljang.shop.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shop.delivery.entity.DeliveryPayment;

import java.util.Optional;

@Repository
public interface DeliveryPaymentRepository extends JpaRepository<DeliveryPayment, Long> {
    Optional<DeliveryPayment> findTopByDeliveryOrderOrderByIdDesc(DeliveryOrder deliveryOrder);
}
