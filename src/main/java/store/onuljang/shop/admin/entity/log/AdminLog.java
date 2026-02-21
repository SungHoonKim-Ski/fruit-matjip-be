package store.onuljang.shop.admin.entity.log;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.shop.admin.event.AdminLogEvent;
import store.onuljang.shared.entity.base.BaseLogEntity;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "admin_logs")
@Builder
public class AdminLog extends BaseLogEntity {

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "path", nullable = false, length = 255)
    private String path;

    @Column(name = "method", nullable = false, length = 10)
    private String method;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Column(name = "request_id", length = 64)
    private String requestId;

    public static AdminLog from(AdminLogEvent e) {
        return AdminLog.builder()
            .adminId(e.adminId())
            .path(truncate(e.path(), 255))
            .method(e.method())
            .status(e.status())
            .durationMs(e.durationMs())
            .requestId(truncate(e.requestId(), 64))
            .build();
    }

    private static String truncate(String s, int max) {
        if (s == null)
            return null;
        return (s.length() > max) ? s.substring(0, max) : s;
    }
}
