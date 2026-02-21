package store.onuljang.courier.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierPayment;

public interface CourierPaymentRepository extends JpaRepository<CourierPayment, Long> {

    Optional<CourierPayment> findTopByCourierOrderOrderByIdDesc(CourierOrder courierOrder);
}
