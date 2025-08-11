package store.onuljang.repository.entity.log;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.base.BaseLogEntity;

@NoArgsConstructor
@Entity
@Table(name = "admin_logs")
public class AdminLog extends BaseLogEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "request_api", nullable = false)
    private String requestApi;

    @Builder
    public AdminLog(Admin admin, String requestApi) {
        this.admin = admin;
        this.requestApi = requestApi;
    }
}