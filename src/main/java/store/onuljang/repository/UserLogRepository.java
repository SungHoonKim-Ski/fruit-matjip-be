package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.onuljang.repository.entity.log.AdminLog;
import store.onuljang.repository.entity.log.UserLog;

@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {
}
