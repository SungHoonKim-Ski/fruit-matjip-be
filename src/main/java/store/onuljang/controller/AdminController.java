package store.onuljang.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.AdminAppService;
import store.onuljang.controller.request.AdminSignupRequest;
import store.onuljang.service.dto.AdminUserDetails;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Validated
public class AdminController {
    AdminAppService adminAppService;

    // login => AdminLoginFilter

    @PostMapping("/signup")
    public ResponseEntity<Long> sighup(@RequestBody @Valid AdminSignupRequest request) {
        long res = adminAppService.signUp(request);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateSession(@AuthenticationPrincipal AdminUserDetails admin) {
        adminAppService.validate(admin.getAdminId());

        return ResponseEntity.ok().build();
    }
}
