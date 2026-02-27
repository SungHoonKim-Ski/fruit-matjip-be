package store.onuljang.shop.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.entity.enums.DeliveryStatus;

import java.time.LocalDate;
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

    List<DeliveryOrder> findByDeliveryDateAndStatusIn(
            LocalDate deliveryDate, List<DeliveryStatus> statuses);
}
