package store.onuljang.shop.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.onuljang.shop.admin.entity.log.AdminProductLog;

@Repository
public interface AdminProductLogRepository extends JpaRepository<AdminProductLog, Long> {
}
