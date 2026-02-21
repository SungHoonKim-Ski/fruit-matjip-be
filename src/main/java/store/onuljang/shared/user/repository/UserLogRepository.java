package store.onuljang.shared.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.onuljang.shared.user.entity.log.UserLog;

@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {
}
