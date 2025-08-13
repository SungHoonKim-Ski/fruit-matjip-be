package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.auth.AdminAuthenticationToken;
import store.onuljang.controller.request.AdminSignupRequest;
import store.onuljang.exception.ExistAdminException;
import store.onuljang.exception.UnauthorizedException;
import store.onuljang.repository.entity.Admin;
import store.onuljang.service.AdminService;

import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAppService {
    AdminService adminService;
    PasswordEncoder passwordEncoder;

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

    @Transactional
    public long signUp(AdminSignupRequest request) {
        validateEmail(request.email());

        String password = passwordEncoder.encode(request.password());

        Admin admin = AdminSignupRequest.toEntity(request, password);

        return adminService.save(admin).getId();
    }

    private void validateEmail(String email) {
        Optional<Admin> admin = adminService.existEmail(email);
        if (admin.isPresent()) {
            throw new ExistAdminException("이미 존재하는 아이디입니다");
        }
    }
}
