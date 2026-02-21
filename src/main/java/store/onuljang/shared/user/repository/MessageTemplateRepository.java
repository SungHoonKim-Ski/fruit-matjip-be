package store.onuljang.shared.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.onuljang.shared.repository.entity.MessageTemplate;
import store.onuljang.shared.entity.enums.MessageType;

import java.util.Optional;

@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {
    Optional<MessageTemplate> findByMessageType(MessageType type);
}
