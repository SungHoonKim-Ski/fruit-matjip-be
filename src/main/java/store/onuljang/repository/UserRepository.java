package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.Users;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findBySocialId(String socialId);
    Optional<Users> findByUid(String Uid);
    Optional<Users> findByName(String name);
}

