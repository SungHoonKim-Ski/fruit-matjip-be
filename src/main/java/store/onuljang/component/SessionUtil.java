package store.onuljang.component;

import lombok.experimental.UtilityClass;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import store.onuljang.config.auth.AdminAuthenticationToken;

@UtilityClass
public class SessionUtil {
    public static Long getAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AdminAuthenticationToken token) || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("유효한 관리자 인증 정보가 없습니다");
        }

        Long adminId = token.getAdminId();
        if (adminId == null || adminId <= 0) {
            throw new AccessDeniedException("관리자 ID가 없습니다");
        }

        return adminId;
    }
}
