package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.AdminUserAppService;

import java.util.UUID;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Validated
public class AdminUserController {

    AdminUserAppService adminUserAppService;

    @PostMapping("/user/warn/{uid}")
    public ResponseEntity<Void> warnUser(@PathVariable("uid") UUID uid) {
        adminUserAppService.warn(uid);

        return ResponseEntity.ok().build();
    }
}