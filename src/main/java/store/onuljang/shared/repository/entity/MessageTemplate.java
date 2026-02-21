package store.onuljang.shared.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.shared.entity.base.BaseEntity;
import store.onuljang.shared.entity.enums.MessageType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "message_template")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE message_template SET deleted_at = NOW() WHERE id = ?")
public class MessageTemplate extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false)
    private String body;

    @Column(name = "message_type", nullable = false)
    @Enumerated(EnumType.STRING)
    MessageType messageType;

    @Column(nullable = false)
    private int priority;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public MessageTemplate(String title, String body, int priority) {
        this.title = title;
        this.body = body;
        this.priority = priority;
    }

    public void update(String title, String body) {
        this.title = title;
        this.body = body;
    }
}
