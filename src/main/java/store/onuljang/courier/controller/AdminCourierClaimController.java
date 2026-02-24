package store.onuljang.courier.controller;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.courier.appservice.CourierClaimAppService;
import store.onuljang.courier.dto.CourierClaimApproveRequest;
import store.onuljang.courier.dto.CourierClaimListResponse;
import store.onuljang.courier.dto.CourierClaimResponse;
import store.onuljang.shared.entity.enums.CourierClaimStatus;

@RestController
@RequestMapping("/api/admin/courier/claims")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AdminCourierClaimController {

    CourierClaimAppService courierClaimAppService;

    @GetMapping
    public ResponseEntity<CourierClaimListResponse> getClaims(
            @RequestParam(required = false) CourierClaimStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(courierClaimAppService.getAdminClaims(status, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourierClaimResponse> getClaim(@PathVariable("id") Long id) {
        return ResponseEntity.ok(courierClaimAppService.getAdminClaim(id));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<CourierClaimResponse> approveClaim(
            @PathVariable("id") Long id,
            @Valid @RequestBody CourierClaimApproveRequest request) {
        return ResponseEntity.ok(courierClaimAppService.approveClaim(id, request));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<CourierClaimResponse> rejectClaim(
            @PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        String adminNote = body.getOrDefault("admin_note", body.getOrDefault("adminNote", ""));
        return ResponseEntity.ok(courierClaimAppService.rejectClaim(id, adminNote));
    }

    @PatchMapping("/{id}/order-status")
    public ResponseEntity<CourierClaimResponse> updateOrderStatus(
            @PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.getOrDefault("order_status", body.getOrDefault("orderStatus", ""));
        return ResponseEntity.ok(courierClaimAppService.updateClaimOrderStatus(id, newStatus));
    }
}
