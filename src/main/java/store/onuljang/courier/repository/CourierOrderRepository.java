package store.onuljang.courier.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.shared.user.entity.Users;

public interface CourierOrderRepository extends JpaRepository<CourierOrder, Long> {

    Optional<CourierOrder> findByDisplayCode(String displayCode);

    Optional<CourierOrder> findByUserAndIdempotencyKey(Users user, String idempotencyKey);

    boolean existsByDisplayCode(String displayCode);
}
