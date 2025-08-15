package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.AdminSignupRequest;
import store.onuljang.exception.ExistAdminException;
import store.onuljang.repository.entity.Admin;
import store.onuljang.service.AdminService;

import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAppService {
    AdminService adminService;
    PasswordEncoder passwordEncoder;

    public void validate(long adminId) {
        adminService.findById(adminId);
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
            throw new ExistAdminException("이미 존재하는 아이디입니다.");
        }
    }
}
