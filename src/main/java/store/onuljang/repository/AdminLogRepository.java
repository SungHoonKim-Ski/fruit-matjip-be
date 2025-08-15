package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.onuljang.repository.entity.log.AdminLog;

@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
}
