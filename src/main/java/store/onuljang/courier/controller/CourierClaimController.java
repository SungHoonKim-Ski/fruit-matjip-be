package store.onuljang.courier.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.courier.appservice.CourierClaimAppService;
import store.onuljang.courier.dto.CourierClaimRequest;
import store.onuljang.courier.dto.CourierClaimResponse;

@RestController
@RequestMapping("/api/auth/courier/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class CourierClaimController {

    CourierClaimAppService courierClaimAppService;

    @PostMapping("/{displayCode}/claim")
    public ResponseEntity<CourierClaimResponse> createClaim(
            Authentication auth,
            @PathVariable("displayCode") String displayCode,
            @Valid @RequestBody CourierClaimRequest request) {
        String uid = auth.getName();
        return ResponseEntity.ok(courierClaimAppService.createClaim(uid, displayCode, request));
    }

    @GetMapping("/{displayCode}/claims")
    public ResponseEntity<List<CourierClaimResponse>> getClaims(
            Authentication auth, @PathVariable("displayCode") String displayCode) {
        String uid = auth.getName();
        return ResponseEntity.ok(courierClaimAppService.getClaimsByOrder(uid, displayCode));
    }
}
