package store.onuljang.courier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.courier.entity.CourierOrderItem;

public interface CourierOrderItemRepository extends JpaRepository<CourierOrderItem, Long> {}
