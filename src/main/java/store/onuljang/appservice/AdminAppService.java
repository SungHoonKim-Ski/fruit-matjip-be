package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import store.onuljang.config.auth.AdminAuthenticationToken;
import store.onuljang.exception.UnauthorizedException;
import store.onuljang.service.AdminService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAppService {
    AdminService adminService;

    public void validate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("인증되지 않은 사용자입니다");
        }

        if (authentication instanceof AdminAuthenticationToken authToken) {
            long id = authToken.getAdminId();
            adminService.findById(id);
        } else {
            throw new UnauthorizedException("유효하지 않은 인증 토큰입니다");
        }
    }
}
