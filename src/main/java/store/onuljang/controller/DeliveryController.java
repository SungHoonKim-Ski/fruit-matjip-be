package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.DeliveryAppService;
import store.onuljang.controller.request.DeliveryInfoRequest;
import store.onuljang.controller.request.DeliveryFeeRequest;
import store.onuljang.controller.request.DeliveryReadyRequest;
import store.onuljang.controller.response.DeliveryConfigResponse;
import store.onuljang.controller.response.DeliveryFeeResponse;
import store.onuljang.controller.response.DeliveryInfoResponse;
import store.onuljang.controller.response.DeliveryReadyResponse;
import store.onuljang.service.DeliveryConfigService;

@RestController
@RequestMapping("/api/auth/deliveries")
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeliveryController {
    DeliveryAppService deliveryAppService;
    DeliveryConfigService deliveryConfigService;

    @GetMapping("/config")
    public ResponseEntity<DeliveryConfigResponse> getConfig() {
        return ResponseEntity.ok(DeliveryConfigResponse.from(deliveryConfigService.getConfig()));
    }

    @GetMapping("/info")
    public ResponseEntity<DeliveryInfoResponse> getInfo(Authentication auth) {
        String uid = auth.getName();
        DeliveryInfoResponse info = deliveryAppService.getDeliveryInfo(uid);
        return ResponseEntity.ok(info);
    }

    @PutMapping("/info")
    public ResponseEntity<Void> saveInfo(Authentication auth, @RequestBody @Valid DeliveryInfoRequest request) {
        String uid = auth.getName();
        deliveryAppService.saveDeliveryInfo(uid, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fee")
    public ResponseEntity<DeliveryFeeResponse> estimateFee(@RequestBody @Valid DeliveryFeeRequest request) {
        return ResponseEntity.ok(DeliveryFeeResponse.from(
            deliveryAppService.estimateFee(request.latitude(), request.longitude())
        ));
    }

    @PostMapping("/ready")
    public ResponseEntity<DeliveryReadyResponse> ready(Authentication auth,
            @RequestBody @Valid DeliveryReadyRequest request) {
        String uid = auth.getName();
        return ResponseEntity.ok(deliveryAppService.ready(uid, request));
    }

    @GetMapping("/approve")
    public ResponseEntity<Void> approve(Authentication auth,
            @RequestParam("order_id") @NotBlank String orderIdentifier,
            @RequestParam("pg_token") @NotBlank String pgToken) {
        String uid = auth.getName();
        deliveryAppService.approve(uid, orderIdentifier, pgToken);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cancel")
    public ResponseEntity<Void> cancel(Authentication auth,
            @RequestParam("order_id") @NotBlank String orderIdentifier) {
        String uid = auth.getName();
        deliveryAppService.cancel(uid, orderIdentifier);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/fail")
    public ResponseEntity<Void> fail(Authentication auth,
            @RequestParam("order_id") @NotBlank String orderIdentifier) {
        String uid = auth.getName();
        deliveryAppService.fail(uid, orderIdentifier);
        return ResponseEntity.ok().build();
    }
}
