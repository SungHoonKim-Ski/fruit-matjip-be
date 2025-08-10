package store.onuljang.controller;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.LoginAppService;
import store.onuljang.controller.request.LoginRequest;
import store.onuljang.controller.response.LoginResponse;

@RequestMapping("/api/login")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginController {
    LoginAppService loginAppService;

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        return ResponseEntity.ok(loginAppService.login(request));
    }
}
