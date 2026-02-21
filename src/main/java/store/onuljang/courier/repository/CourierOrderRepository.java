package store.onuljang.courier.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.user.entity.Users;

public interface CourierOrderRepository extends JpaRepository<CourierOrder, Long> {

    Optional<CourierOrder> findByDisplayCode(String displayCode);

    Optional<CourierOrder> findByUserAndIdempotencyKey(Users user, String idempotencyKey);

    boolean existsByDisplayCode(String displayCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM CourierOrder o WHERE o.id = :id")
    Optional<CourierOrder> findByIdForUpdate(@Param("id") Long id);

    Optional<CourierOrder> findByDisplayCodeAndUser(String displayCode, Users user);

    List<CourierOrder> findByUserAndStatus(Users user, CourierOrderStatus status);

    @Query(
            "SELECT o FROM CourierOrder o WHERE o.user = :user AND (:cursor IS NULL OR o.id < :cursor)"
                    + " ORDER BY o.id DESC")
    List<CourierOrder> findByUserWithCursor(
            @Param("user") Users user, @Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT o FROM CourierOrder o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<CourierOrder> findByIdWithItems(@Param("id") Long id);

    @Query(
            "SELECT o FROM CourierOrder o WHERE (:status IS NULL OR o.status = :status)"
                    + " ORDER BY o.id DESC")
    List<CourierOrder> findAllByStatusOrderByIdDesc(
            @Param("status") CourierOrderStatus status, Pageable pageable);

    List<CourierOrder> findAllByIdIn(List<Long> ids);
}
