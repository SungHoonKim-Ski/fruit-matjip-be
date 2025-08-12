package store.onuljang.controller;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.AuthAppService;
import store.onuljang.controller.request.LoginRequest;
import store.onuljang.controller.response.LoginResponse;
import store.onuljang.service.dto.JwtToken;

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
    public ResponseEntity<String> reissueTokens(
        @RequestHeader(value="Authorization", required=false) String authorization,
        @CookieValue(value="REFRESH_TOKEN", required=false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok().body(authAppService.refresh(authorization, refreshToken));
    }

    @PatchMapping("/auth/name/{name}")
    public ResponseEntity<?> modifyName(@PathVariable String name) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/auth/name/{name}")
    public ResponseEntity<?> existName(@PathVariable String name) {
        return ResponseEntity.ok().build();
    }
}
