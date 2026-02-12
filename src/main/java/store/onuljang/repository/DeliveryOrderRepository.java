package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {
    Optional<DeliveryOrder> findByIdAndUser(Long id, Users user);

    Optional<DeliveryOrder> findByDisplayCodeAndUser(String displayCode, Users user);

    Optional<DeliveryOrder> findByUserAndIdempotencyKey(Users user, String idempotencyKey);

    List<DeliveryOrder> findByStatusAndCreatedAtBefore(DeliveryStatus status, LocalDateTime before);

    List<DeliveryOrder> findByUserAndStatus(Users user, DeliveryStatus status);

    List<DeliveryOrder> findByStatusAndAcceptedAtBefore(DeliveryStatus status, LocalDateTime cutoff);

    List<DeliveryOrder> findByStatusAndKakaoTidIsNotNullAndCreatedAtBefore(
        DeliveryStatus status, LocalDateTime before);

    boolean existsByDisplayCode(String displayCode);
}
