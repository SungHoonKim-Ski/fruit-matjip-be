package store.onuljang.courier.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
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

    @Query("SELECT o FROM CourierOrder o WHERE o.user = :user"
            + " AND o.createdAt >= :start AND o.createdAt < :end"
            + " ORDER BY o.id DESC")
    List<CourierOrder> findByUserAndDateRange(
            @Param("user") Users user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT o FROM CourierOrder o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<CourierOrder> findByIdWithItems(@Param("id") Long id);

    @Query(
            "SELECT o FROM CourierOrder o WHERE (:status IS NULL OR o.status = :status)"
                    + " ORDER BY o.id DESC")
    Page<CourierOrder> findAllByStatusOrderByIdDesc(
            @Param("status") CourierOrderStatus status, Pageable pageable);

    List<CourierOrder> findAllByIdIn(List<Long> ids);

    List<CourierOrder> findByStatusInAndWaybillNumberIsNotNull(List<CourierOrderStatus> statuses);

    @Query("SELECT DISTINCT o FROM CourierOrder o " +
           "JOIN FETCH o.items i " +
           "WHERE o.paidAt >= :startDateTime AND o.paidAt < :endDateTime " +
           "AND o.status IN :statuses " +
           "ORDER BY o.id DESC")
    List<CourierOrder> findByDateRangeAndStatuses(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("statuses") List<CourierOrderStatus> statuses);

    @Query("SELECT DISTINCT o FROM CourierOrder o " +
           "JOIN FETCH o.items i " +
           "WHERE o.paidAt >= :startDateTime AND o.paidAt < :endDateTime " +
           "AND o.status IN :statuses " +
           "AND EXISTS (SELECT 1 FROM CourierOrderItem oi WHERE oi.courierOrder = o AND oi.courierProduct.id = :productId) " +
           "ORDER BY o.id DESC")
    List<CourierOrder> findByDateRangeAndStatusesAndProduct(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("statuses") List<CourierOrderStatus> statuses,
            @Param("productId") Long productId);
}
