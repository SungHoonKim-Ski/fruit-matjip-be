package store.onuljang.controller;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.AuthAppService;
import store.onuljang.controller.request.LoginRequest;
import store.onuljang.controller.response.LoginResponse;

@RequestMapping("/api")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginController {
    AuthAppService authAppService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authAppService.socialLogin(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> reissueTokens() {
        return ResponseEntity.ok().build();
    }
}
