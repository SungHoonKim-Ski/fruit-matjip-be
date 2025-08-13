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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.AdminAppService;
import store.onuljang.controller.request.AdminSignupRequest;
import store.onuljang.repository.AdminRepository;
import store.onuljang.repository.entity.Admin;

import java.util.Optional;

record AdminLoginRequest(String email, String password) {}

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminController {
    AuthenticationManager authenticationManager;
    SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    AdminRepository adminRepository;
    AdminAppService adminAppService;

    @PostMapping("/login")
    public ResponseEntity<Long> login(HttpServletRequest request, HttpServletResponse response, @RequestBody AdminLoginRequest req) {
        Authentication auth = new UsernamePasswordAuthenticationToken(req.email(), req.password());

        var authRes = authenticationManager.authenticate(auth);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authRes);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        securityContextRepository.saveContext(context, request, response);
        Admin admin = adminRepository.findByEmail(req.email())
            .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));

        session.setAttribute("adminId", admin.getId());

        return ResponseEntity.ok(admin.getId());
    }

    @PostMapping("/signup")
    public ResponseEntity<Long> sighup(@RequestBody AdminSignupRequest request) {
        long res = adminAppService.signUp(request);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateSession() {
        adminAppService.validate();

        return ResponseEntity.ok().build();
    }
}
