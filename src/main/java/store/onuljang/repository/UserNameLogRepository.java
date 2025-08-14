package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.log.UserNameLog;

public interface UserNameLogRepository extends JpaRepository<UserNameLog, Long> {
}
