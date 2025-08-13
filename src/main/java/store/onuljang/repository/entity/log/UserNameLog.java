package store.onuljang.repository.entity.log;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.base.BaseLogEntity;

@NoArgsConstructor
@Entity
@Table(name = "user_name_logs")
public class UserNameLog extends BaseLogEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uid", nullable = false)
    private Users user;

    @Column(name = "name_before", nullable = false)
    private String nameBefore;

    @Column(name = "name_after", nullable = false)
    private String nameAfter;
}