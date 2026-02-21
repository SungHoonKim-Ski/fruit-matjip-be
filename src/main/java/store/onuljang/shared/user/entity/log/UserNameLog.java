package store.onuljang.shared.user.entity.log;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import store.onuljang.shared.entity.base.BaseLogEntity;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_name_logs")
@Builder
public class UserNameLog extends BaseLogEntity {
    @JoinColumn(name = "user_uid", nullable = false)
    private String userUid;

    @Column(name = "name_before", nullable = false)
    private String nameBefore;

    @Column(name = "name_after", nullable = false)
    private String nameAfter;
}
