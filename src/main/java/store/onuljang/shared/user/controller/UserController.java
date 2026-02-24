package store.onuljang.shared.user.controller;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.shared.user.appservice.UserAppService;
import store.onuljang.shared.user.dto.PointBalanceResponse;
import store.onuljang.shared.user.dto.PointHistoryResponse;
import store.onuljang.shared.user.dto.PointTransactionResponse;
import store.onuljang.shared.user.dto.UserMeResponse;
import store.onuljang.shared.user.dto.UserMessageResponse;
import store.onuljang.shared.user.service.UserPointService;

@RequestMapping("/api/auth")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Validated
public class UserController {
    UserAppService userAppService;
    UserPointService userPointService;

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

    @GetMapping("/message")
    public ResponseEntity<UserMessageResponse> getMessage(Authentication auth) {
        String uid = auth.getName();

        return ResponseEntity.ok(userAppService.getMessage(uid));
    }

    @PatchMapping("/message/{messageId}")
    public ResponseEntity<Void> messageReceived(Authentication auth, @NotNull @Positive @PathVariable long messageId) {
        String uid = auth.getName();
        userAppService.messageReceived(uid, messageId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserMeResponse> getUserMe(Authentication auth) {
        String uid = auth.getName();

        return ResponseEntity.ok(userAppService.getUserMe(uid));
    }

    @GetMapping("/points")
    public ResponseEntity<PointBalanceResponse> getPointBalance(Authentication auth) {
        String uid = auth.getName();
        BigDecimal balance = userPointService.getBalance(uid);
        List<PointTransactionResponse> recent = userPointService.getRecentHistory(uid);
        return ResponseEntity.ok(new PointBalanceResponse(balance, recent));
    }

    @GetMapping("/points/history")
    public ResponseEntity<PointHistoryResponse> getPointHistory(
        Authentication auth,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        String uid = auth.getName();
        return ResponseEntity.ok(
            PointHistoryResponse.from(userPointService.getHistory(uid, PageRequest.of(page, size)))
        );
    }
}
