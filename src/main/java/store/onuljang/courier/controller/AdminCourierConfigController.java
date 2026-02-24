package store.onuljang.courier.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.courier.appservice.CourierAdminConfigAppService;
import store.onuljang.courier.dto.CourierConfigAdminResponse;
import store.onuljang.courier.dto.CourierConfigUpdateRequest;

@RestController
@RequestMapping("/api/admin/courier")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AdminCourierConfigController {

    CourierAdminConfigAppService courierAdminConfigAppService;

    @GetMapping("/config")
    public ResponseEntity<CourierConfigAdminResponse> getConfig() {
        return ResponseEntity.ok(courierAdminConfigAppService.getConfig());
    }

    @PutMapping("/config")
    public ResponseEntity<CourierConfigAdminResponse> updateConfig(
            @Valid @RequestBody CourierConfigUpdateRequest request) {
        return ResponseEntity.ok(courierAdminConfigAppService.updateConfig(request));
    }
}
