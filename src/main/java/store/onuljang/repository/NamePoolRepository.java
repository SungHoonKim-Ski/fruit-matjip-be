package store.onuljang.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import store.onuljang.repository.entity.NamePool;

import java.util.Optional;

public interface NamePoolRepository extends JpaRepository<NamePool, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "select n " +
        "from NamePool n " +
        "where n.id = :id"
    )
    Optional<NamePool> findByIdWithLock(long id);
}
