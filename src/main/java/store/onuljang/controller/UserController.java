package store.onuljang.controller;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.UserAppService;
import store.onuljang.controller.response.UserMessageResponse;

@RequestMapping("/api/auth")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Validated
public class UserController {
    UserAppService userAppService;

    @PatchMapping("/name/{name}")
    public ResponseEntity<Void> modifyName(
        Authentication auth,
        @PathVariable
        @NotBlank
        @Size(min = 3, max = 10, message = "이름은 3~10자여야 합니다.")
        @Pattern(regexp = "^[A-Za-z0-9\\uAC00-\\uD7A3]+$", message = "이름은 한글, 영어, 숫자만 허용됩니다.")
        String name
    ) {
        String uid = auth.getName();

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

    @Deprecated
    @GetMapping("/reservation/self-pick")
    public ResponseEntity<Boolean> canSelfPick(Authentication auth) {
        String uid = auth.getName();

        return ResponseEntity.ok(userAppService.canSelfPick(uid));
    }

    @GetMapping("/message")
    public ResponseEntity<UserMessageResponse> getMessage(Authentication auth) {
        String uid = auth.getName();

        return ResponseEntity.ok(userAppService.getMessage(uid));
    }

    @PatchMapping("/message/{messageId}")
    public ResponseEntity<Void> messageReceived(@NotNull @Positive @PathVariable long messageId) {
        userAppService.messageReceived(messageId);

        return ResponseEntity.ok().build();
    }
}
