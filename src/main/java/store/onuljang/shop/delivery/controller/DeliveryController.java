package store.onuljang.shop.delivery.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.shop.delivery.appservice.DeliveryAppService;
import store.onuljang.shop.delivery.dto.DeliveryInfoRequest;
import store.onuljang.shop.delivery.dto.DeliveryFeeRequest;
import store.onuljang.shop.delivery.dto.DeliveryReadyRequest;
import store.onuljang.shop.delivery.dto.DeliveryConfigResponse;
import store.onuljang.shop.delivery.dto.DeliveryFeeResponse;
import store.onuljang.shop.delivery.dto.DeliveryInfoResponse;
import store.onuljang.shop.delivery.dto.DeliveryReadyResponse;
import store.onuljang.shop.delivery.service.DeliveryConfigService;

@RestController
@RequestMapping("/api/store/auth/deliveries")
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
