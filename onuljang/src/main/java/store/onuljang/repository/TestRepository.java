package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.onuljang.repository.entity.TestEntity;

@Repository
public interface TestRepository extends JpaRepository<TestEntity, Long> {

}
