package store.onuljang.shop.admin.controller;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import store.onuljang.shop.admin.appservice.AdminDeliveryAppService;
import store.onuljang.shop.admin.dto.AdminDeliveryAcceptRequest;
import store.onuljang.shop.admin.dto.AdminDeliveryConfigRequest;
import store.onuljang.shop.admin.dto.AdminDeliveryListResponse;
import store.onuljang.shop.delivery.dto.DeliveryConfigResponse;
import store.onuljang.shared.entity.enums.DeliveryStatus;
import store.onuljang.shop.delivery.service.AdminDeliverySseService;
import store.onuljang.shop.delivery.service.DeliveryConfigService;
import store.onuljang.shop.delivery.service.DeliveryOrderService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/shop/deliveries")
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDeliveryController {
    DeliveryOrderService deliveryOrderService;
    AdminDeliverySseService adminDeliverySseService;
    AdminDeliveryAppService adminDeliveryAppService;
    DeliveryConfigService deliveryConfigService;

    @GetMapping
    public ResponseEntity<AdminDeliveryListResponse> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate date) {
        return ResponseEntity.ok(AdminDeliveryListResponse.from(deliveryOrderService.findAllByDeliveryDateWithProductAll(date)));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return adminDeliverySseService.subscribe();
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<Void> accept(
            @PathVariable("id") long id,
            @RequestBody @Validated AdminDeliveryAcceptRequest request) {
        adminDeliveryAppService.accept(id, request.estimatedMinutes());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<Void> updateStatus(
            @PathVariable("id") long id, @PathVariable("status") String status) {
        DeliveryStatus next = DeliveryStatus.valueOf(status.toUpperCase());
        adminDeliveryAppService.updateStatus(id, next);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/config")
    public ResponseEntity<DeliveryConfigResponse> getConfig() {
        return ResponseEntity.ok(DeliveryConfigResponse.from(deliveryConfigService.getConfig()));
    }

    @PutMapping("/config")
    public ResponseEntity<DeliveryConfigResponse> updateConfig(@RequestBody @Validated AdminDeliveryConfigRequest request) {
        return ResponseEntity.ok(DeliveryConfigResponse.from(deliveryConfigService.update(request)));
    }
}
