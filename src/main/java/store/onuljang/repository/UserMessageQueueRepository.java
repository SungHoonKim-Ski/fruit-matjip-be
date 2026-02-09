package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import store.onuljang.repository.entity.UserMessageQueue;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserMessageQueueRepository extends JpaRepository<UserMessageQueue, Long> {
    @Query(value = """
    select m.*
    from user_message_queue m
    left join message_template t on m.message_template_id = t.id
    where m.user_uid = :uid
      and m.status = 'PENDING'
      and (m.valid_from is null or m.valid_from <= :now)
      and (m.valid_until is null or m.valid_until >= :now)
    order by t.priority limit 1
    """,nativeQuery = true)
    Optional<UserMessageQueue> findFirstPendingByUidWithMessageTemplate(@Param("uid") String uid, @Param("now") LocalDateTime now);
}
