package store.onuljang.shared.user.entity.log;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import store.onuljang.shared.user.event.UserLogEvent;
import store.onuljang.shared.entity.base.BaseLogEntity;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_logs")
@Builder
public class UserLog extends BaseLogEntity {

    @Column(name = "user_uid", nullable = false, length = 36)
    private String userUid;

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

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    public static UserLog from(UserLogEvent e) {
        return UserLog.builder()
            .userUid(e.userUid())
            .path(truncate(e.path(), 255))
            .method(e.method())
            .status(e.status())
            .durationMs(e.durationMs())
            .requestId(truncate(e.requestId(), 64))
            .clientIp(truncate(e.clientIp(), 45))
            .build();
    }

    private static String truncate(String s, int max) {
        if (s == null)
            return null;
        return (s.length() > max) ? s.substring(0, max) : s;
    }
}
