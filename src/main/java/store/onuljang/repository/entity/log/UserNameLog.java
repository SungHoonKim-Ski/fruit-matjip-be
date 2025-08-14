package store.onuljang.repository.entity.log;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.base.BaseLogEntity;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_name_logs")
@Builder
public class UserNameLog extends BaseLogEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users user;

    @Column(name = "name_before", nullable = false)
    private String nameBefore;

    @Column(name = "name_after", nullable = false)
    private String nameAfter;
}