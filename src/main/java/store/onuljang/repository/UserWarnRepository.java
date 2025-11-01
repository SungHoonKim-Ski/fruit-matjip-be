package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.onuljang.repository.entity.UserWarn;

@Repository
public interface UserWarnRepository extends JpaRepository<UserWarn, Long> {
}
