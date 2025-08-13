package store.onuljang.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/login")
    public ResponseEntity<Long> login(HttpServletRequest request, HttpServletResponse response, @RequestBody AdminLoginRequest req) {
        Authentication auth = new UsernamePasswordAuthenticationToken(req.email(), req.password());

        var authRes = authenticationManager.authenticate(auth);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authRes);
        SecurityContextHolder.setContext(context);

        request.getSession(true);

        securityContextRepository.saveContext(context, request, response);
        Admin admin = adminRepository.findByEmail(req.email())
            .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
        return ResponseEntity.ok(admin.getId());
    }

    @PostMapping("/sighup")
    public ResponseEntity<Void> sighup() {
        return ResponseEntity.ok().build();
    }
}
