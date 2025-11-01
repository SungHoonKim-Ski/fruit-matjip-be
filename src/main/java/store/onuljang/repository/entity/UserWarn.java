package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.repository.entity.base.BaseEntity;
import store.onuljang.repository.entity.base.BaseLogEntity;
import store.onuljang.repository.entity.enums.UserWarnReason;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_warn")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user_warn SET deleted_at = NOW() WHERE id = ?")
public class UserWarn extends BaseLogEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_uid",
        referencedColumnName = "uid",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_user_warn__user")
    )
    private Users user;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 255, nullable = false)
    private UserWarnReason reason;

    @Builder
    public UserWarn(Users user, UserWarnReason reason) {
        this.user = user;
        this.reason = reason;
    }
}
