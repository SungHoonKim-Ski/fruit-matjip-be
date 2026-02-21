package store.onuljang.shared.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.shared.user.entity.UserDeliveryInfo;
import store.onuljang.shared.user.entity.Users;

import java.util.Optional;

public interface UserDeliveryInfoRepository extends JpaRepository<UserDeliveryInfo, Long> {
    Optional<UserDeliveryInfo> findByUser(Users user);
}
