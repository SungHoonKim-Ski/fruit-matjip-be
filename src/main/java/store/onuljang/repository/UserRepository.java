package store.onuljang.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import store.onuljang.repository.entity.Users;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findBySocialId(String socialId);
    Optional<Users> findByUid(String uid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = " select u from Users u where u.uid = :uid")
    Optional<Users> findByUidWithLock(String uid);
    Optional<Users> findByName(String name);
}

