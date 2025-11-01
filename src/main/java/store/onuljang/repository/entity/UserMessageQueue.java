package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.repository.entity.base.BaseEntity;
import store.onuljang.repository.entity.base.BaseLogEntity;
import store.onuljang.repository.entity.enums.MessageType;
import store.onuljang.repository.entity.enums.UserMessageStatus;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "user_message_queue",
    indexes = {
        @Index(name = "idx_umq_user_status", columnList = "user_uid, status")
    })
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user_message_queue SET deleted_at = NOW() WHERE id = ?")
public class UserMessageQueue extends BaseLogEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_uid",
        referencedColumnName = "uid",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_umq_user"),
        insertable = false, //
        updatable = false // userUid를 String으로 생성하기 위해 추가한 옵션
    )
    private Users user;

    @Column(name = "user_uid", nullable = false)
    private String userUid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "message_template_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_umq_template")
    )
    private MessageTemplate messageTemplate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserMessageStatus status = UserMessageStatus.PENDING;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Builder
    public UserMessageQueue(String userUid, MessageTemplate template) {
        this.userUid = userUid;
        this.messageTemplate = template;
    }

    public boolean isValidNow(LocalDateTime now) {
        if (validFrom != null && now.isBefore(validFrom)) return false;
        if (validUntil != null && now.isAfter(validUntil)) return false;
        return true;
    }

    public void markSent(LocalDateTime now) {
        this.status = UserMessageStatus.SENT;
        this.sentAt = now;
    }

    public void markReceived(LocalDateTime now) {
        this.receivedAt = now;
    }

    public void expire() {
        this.status = UserMessageStatus.EXPIRED;
    }

    public String getMessageTemplateTitle() {
        return messageTemplate.getTitle();
    }

    public String getMessageTemplateBody() {
        return messageTemplate.getBody();
    }
}