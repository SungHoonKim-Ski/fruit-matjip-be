package store.onuljang.courier.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.courier.appservice.CourierAdminConfigAppService;
import store.onuljang.courier.dto.CourierConfigAdminResponse;
import store.onuljang.courier.dto.CourierConfigUpdateRequest;
import store.onuljang.courier.dto.ShippingFeePolicyListResponse;
import store.onuljang.courier.dto.ShippingFeePolicyRequest;
import store.onuljang.courier.dto.ShippingFeeTemplateListResponse;
import store.onuljang.courier.dto.ShippingFeeTemplateRequest;
import store.onuljang.courier.dto.ShippingFeeTemplateResponse;

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

    @GetMapping("/shipping-fee-policies")
    public ResponseEntity<ShippingFeePolicyListResponse> getShippingFeePolicies() {
        return ResponseEntity.ok(courierAdminConfigAppService.getShippingFeePolicies());
    }

    @PutMapping("/shipping-fee-policies")
    public ResponseEntity<ShippingFeePolicyListResponse> replaceShippingFeePolicies(
            @Valid @RequestBody List<ShippingFeePolicyRequest> requests) {
        return ResponseEntity.ok(courierAdminConfigAppService.replaceShippingFeePolicies(requests));
    }

    @GetMapping("/shipping-fee-templates")
    public ResponseEntity<ShippingFeeTemplateListResponse> getShippingFeeTemplates() {
        return ResponseEntity.ok(courierAdminConfigAppService.getShippingFeeTemplates());
    }

    @PostMapping("/shipping-fee-templates")
    public ResponseEntity<ShippingFeeTemplateResponse> createShippingFeeTemplate(
            @Valid @RequestBody ShippingFeeTemplateRequest request) {
        return ResponseEntity.ok(courierAdminConfigAppService.createShippingFeeTemplate(request));
    }

    @PutMapping("/shipping-fee-templates/{id}")
    public ResponseEntity<ShippingFeeTemplateResponse> updateShippingFeeTemplate(
            @PathVariable("id") Long id, @Valid @RequestBody ShippingFeeTemplateRequest request) {
        return ResponseEntity.ok(
                courierAdminConfigAppService.updateShippingFeeTemplate(id, request));
    }

    @DeleteMapping("/shipping-fee-templates/{id}")
    public ResponseEntity<Void> deleteShippingFeeTemplate(@PathVariable("id") Long id) {
        courierAdminConfigAppService.deleteShippingFeeTemplate(id);
        return ResponseEntity.ok().build();
    }
}
