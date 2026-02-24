package store.onuljang.shared.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.shared.user.entity.UserPointTransaction;
import store.onuljang.shared.user.entity.Users;

import java.util.List;

public interface UserPointTransactionRepository extends JpaRepository<UserPointTransaction, Long> {
    Page<UserPointTransaction> findByUserOrderByCreatedAtDesc(Users user, Pageable pageable);

    List<UserPointTransaction> findTop5ByUserOrderByCreatedAtDesc(Users user);
}
