package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.NamePool;
import store.onuljang.repository.entity.Users;

import java.util.Optional;

public interface NamePoolRepository extends JpaRepository<NamePool, Long> {

}

