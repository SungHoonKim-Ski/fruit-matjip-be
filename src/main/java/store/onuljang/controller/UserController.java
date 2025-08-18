package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.UserAppService;
import store.onuljang.auth.JwtUtil;

@RequestMapping("/api/auth")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Validated
public class UserController {
    UserAppService userAppService;
    JwtUtil jwtUtil;

    @PatchMapping("/name/{name}")
    public ResponseEntity<Void> modifyName(
        @RequestHeader("Authorization") String authorization,
        @PathVariable
        @NotBlank
        @Size(min = 3, max = 10, message = "이름은 3~10자여야 합니다.")
        @Pattern(regexp = "^[A-Za-z0-9\\uAC00-\\uD7A3]+$", message = "이름은 한글, 영어, 숫자만 허용됩니다.")
        String name
    ) {
        String uid = jwtUtil.getBearerUid(authorization);

        userAppService.modifyName(uid, name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Boolean> existName(
        @PathVariable
        @NotBlank
        @Size(min = 3, max = 10)
        @Pattern(regexp = "^[A-Za-z0-9\\uAC00-\\uD7A3]+$")
        String name
    ) {
        return ResponseEntity.ok(userAppService.existName(name));
    }
}
