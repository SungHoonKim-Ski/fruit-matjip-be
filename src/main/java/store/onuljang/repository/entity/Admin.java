package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.repository.entity.base.BaseEntity;
import store.onuljang.repository.entity.enums.AdminRole;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "admins")
public class Admin extends BaseEntity {

    @Getter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter
    @Column(name = "email", nullable = false)
    private String email;

    @Getter
    @Column(name = "password", nullable = false)
    private String password;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 16)
    @Builder.Default
    private AdminRole role = AdminRole.NONE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}