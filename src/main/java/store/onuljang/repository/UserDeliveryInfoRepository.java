package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.UserDeliveryInfo;
import store.onuljang.repository.entity.Users;

import java.util.Optional;

public interface UserDeliveryInfoRepository extends JpaRepository<UserDeliveryInfo, Long> {
    Optional<UserDeliveryInfo> findByUser(Users user);
}
