package store.onuljang.shared.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.shared.user.entity.log.UserNameLog;

public interface UserNameLogRepository extends JpaRepository<UserNameLog, Long> {
}
