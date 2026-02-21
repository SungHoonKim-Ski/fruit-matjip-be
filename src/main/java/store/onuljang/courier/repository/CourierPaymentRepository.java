package store.onuljang.courier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.courier.entity.CourierPayment;

public interface CourierPaymentRepository extends JpaRepository<CourierPayment, Long> {}
