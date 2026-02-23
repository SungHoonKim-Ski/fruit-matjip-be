package store.onuljang.courier.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.courier.appservice.CourierOrderAppService;
import store.onuljang.courier.dto.*;
import store.onuljang.courier.service.CourierConfigService;
import store.onuljang.courier.service.CourierShippingFeeService;

@RestController
@RequestMapping("/api/auth/courier")
@RequiredArgsConstructor
@Validated
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CourierOrderController {

    CourierOrderAppService courierOrderAppService;
    CourierShippingFeeService courierShippingFeeService;
    CourierConfigService courierConfigService;

    @PostMapping("/orders/ready")
    public ResponseEntity<CourierOrderReadyResponse> ready(
            Authentication auth, @RequestBody @Valid CourierOrderReadyRequest request) {
        String uid = auth.getName();
        return ResponseEntity.ok(courierOrderAppService.ready(uid, request));
    }

    @GetMapping("/orders/approve")
    public ResponseEntity<Void> approve(
            Authentication auth,
            @RequestParam("order_id") @NotBlank String orderIdentifier,
            @RequestParam("pg_token") @NotBlank String pgToken) {
        String uid = auth.getName();
        courierOrderAppService.approve(uid, orderIdentifier, pgToken);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders/cancel")
    public ResponseEntity<Void> cancel(
            Authentication auth,
            @RequestParam("order_id") @NotBlank String orderIdentifier) {
        String uid = auth.getName();
        courierOrderAppService.cancel(uid, orderIdentifier);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders/fail")
    public ResponseEntity<Void> fail(
            Authentication auth,
            @RequestParam("order_id") @NotBlank String orderIdentifier) {
        String uid = auth.getName();
        courierOrderAppService.fail(uid, orderIdentifier);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<List<CourierOrderResponse>> getOrders(
            Authentication auth,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        String uid = auth.getName();
        return ResponseEntity.ok(courierOrderAppService.getOrders(uid, year, month));
    }

    @GetMapping("/orders/{displayCode}")
    public ResponseEntity<CourierOrderDetailResponse> getOrderDetail(
            Authentication auth, @PathVariable @NotBlank String displayCode) {
        String uid = auth.getName();
        return ResponseEntity.ok(courierOrderAppService.getOrderDetail(uid, displayCode));
    }

    @PostMapping("/shipping-fee")
    public ResponseEntity<ShippingFeeResponse> getShippingFee(
            @RequestBody @Valid ShippingFeePreviewRequest request) {
        ShippingFeeResult result = courierShippingFeeService.calculatePreview(request);
        return ResponseEntity.ok(ShippingFeeResponse.from(result));
    }

    @GetMapping("/config")
    public ResponseEntity<CourierConfigResponse> getConfig() {
        return ResponseEntity.ok(CourierConfigResponse.from(courierConfigService.getConfig()));
    }

    @GetMapping("/info")
    public ResponseEntity<CourierInfoResponse> getInfo(Authentication auth) {
        String uid = auth.getName();
        return ResponseEntity.ok(courierOrderAppService.getCourierInfo(uid));
    }

    @PutMapping("/info")
    public ResponseEntity<Void> saveInfo(
            Authentication auth, @RequestBody @Valid CourierInfoRequest request) {
        String uid = auth.getName();
        courierOrderAppService.saveCourierInfo(uid, request);
        return ResponseEntity.ok().build();
    }
}
