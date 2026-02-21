package store.onuljang.courier.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.courier.entity.CourierClaim;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.shared.entity.enums.CourierClaimStatus;

public interface CourierClaimRepository extends JpaRepository<CourierClaim, Long> {

    List<CourierClaim> findByCourierOrderOrderByIdDesc(CourierOrder order);

    @Query(
            "SELECT c FROM CourierClaim c WHERE (:status IS NULL OR c.claimStatus = :status) ORDER BY c.id DESC")
    List<CourierClaim> findAllByStatusOrderByIdDesc(
            @Param("status") CourierClaimStatus status, Pageable pageable);
}
