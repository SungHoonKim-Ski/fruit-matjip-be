package store.onuljang.shared.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.onuljang.shared.user.entity.UserWarn;
import store.onuljang.shared.user.entity.Users;

import java.util.List;

@Repository
public interface UserWarnRepository extends JpaRepository<UserWarn, Long> {
    List<UserWarn> findAllByUserOrderByCreatedAtDesc(Users user);
}
