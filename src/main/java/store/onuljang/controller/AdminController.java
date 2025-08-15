package store.onuljang.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.AdminAppService;
import store.onuljang.controller.request.AdminLoginRequest;
import store.onuljang.controller.request.AdminSignupRequest;
import store.onuljang.repository.AdminRepository;
import store.onuljang.repository.entity.Admin;
import store.onuljang.service.dto.AdminUserDetails;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminController {
    AdminAppService adminAppService;

    // login => AdminLoginFilter

    @PostMapping("/signup")
    public ResponseEntity<Long> sighup(@RequestBody AdminSignupRequest request) {
        long res = adminAppService.signUp(request);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateSession(@AuthenticationPrincipal AdminUserDetails admin) {
        adminAppService.validate(admin.getAdminId());

        return ResponseEntity.ok().build();
    }
}
