package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.onuljang.repository.entity.UserWarn;
import store.onuljang.repository.entity.Users;

import java.util.List;

@Repository
public interface UserWarnRepository extends JpaRepository<UserWarn, Long> {
    List<UserWarn> findAllByUserOrderByCreatedAtDesc(Users user);
}
