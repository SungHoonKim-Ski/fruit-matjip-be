package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.onuljang.repository.entity.log.UserReservationLog;

@Repository
public interface UserReservationLogRepository extends JpaRepository<UserReservationLog, Long> {
}
