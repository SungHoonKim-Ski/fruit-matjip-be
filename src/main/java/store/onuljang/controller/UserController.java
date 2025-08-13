package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.UserAppService;
import store.onuljang.component.JwtUtil;

@RequestMapping("/api")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserController {
    UserAppService userAppService;
    JwtUtil jwtUtil;

    @PatchMapping("/auth/name/{name}")
    public ResponseEntity<Void> modifyName(@RequestHeader("Authorization") String authorization, @PathVariable String name) {
        userAppService.modifyName(jwtUtil.getUid(authorization), name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/auth/name/{name}")
    public ResponseEntity<Boolean> existName(@PathVariable @Valid @NotNull @Min(3) @Max(10) String name) {
        return ResponseEntity.ok(userAppService.existName(name));
    }
}
