package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.repository.entity.base.BaseLogEntity;
import store.onuljang.repository.entity.enums.UserWarnReason;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_warn")
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
