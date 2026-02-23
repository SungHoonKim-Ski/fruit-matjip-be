package store.onuljang.shared.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.shared.user.entity.UserCourierInfo;
import store.onuljang.shared.user.entity.Users;

import java.util.Optional;

public interface UserCourierInfoRepository extends JpaRepository<UserCourierInfo, Long> {
    Optional<UserCourierInfo> findByUser(Users user);
}
