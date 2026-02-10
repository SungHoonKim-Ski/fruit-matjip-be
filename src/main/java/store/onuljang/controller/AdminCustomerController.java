package store.onuljang.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.AdminUserAppService;
import store.onuljang.controller.request.AdminCustomerScrollRequest;
import store.onuljang.controller.response.AdminCustomerScrollResponse;
import store.onuljang.controller.response.AdminCustomerWarnResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Validated
public class AdminCustomerController {

    AdminUserAppService adminUserAppService;

    @PostMapping("/customer/warn/{uid}")
    public ResponseEntity<Void> warnUser(@PathVariable("uid") UUID uid) {
        adminUserAppService.warn(uid);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/customer/warn/reset/{uid}")
    public ResponseEntity<Void> resetWarn(@PathVariable("uid") UUID uid) {
        adminUserAppService.resetWarn(uid);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/{uid}/lift-restriction")
    public ResponseEntity<Void> liftRestriction(@PathVariable("uid") UUID uid) {
        adminUserAppService.liftRestriction(uid);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/customers")
    public ResponseEntity<AdminCustomerScrollResponse> getCustomers(@Valid AdminCustomerScrollRequest request) {
        return ResponseEntity.ok(adminUserAppService.getUsers(request));
    }

    @GetMapping("/customers/warn/{uid}")
    public ResponseEntity<AdminCustomerWarnResponse> getCustomers(@PathVariable("uid") UUID uid) {
        return ResponseEntity.ok(adminUserAppService.getUserWarn(uid));
    }
}
