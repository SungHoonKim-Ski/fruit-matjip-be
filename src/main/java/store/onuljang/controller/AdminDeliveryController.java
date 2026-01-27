package store.onuljang.controller;

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
import store.onuljang.appservice.AdminDeliveryAppService;
import store.onuljang.controller.request.AdminDeliveryConfigRequest;
import store.onuljang.controller.response.AdminDeliveryListResponse;
import store.onuljang.controller.response.DeliveryConfigResponse;
import store.onuljang.repository.entity.enums.DeliveryStatus;
import store.onuljang.service.AdminDeliverySseService;
import store.onuljang.service.DeliveryConfigService;
import store.onuljang.service.DeliveryOrderService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/deliveries")
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
        return ResponseEntity.ok(AdminDeliveryListResponse.from(deliveryOrderService.findAllByDeliveryDate(date)));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return adminDeliverySseService.subscribe();
    }

    @PatchMapping("/{id}/{status}")
    public ResponseEntity<Void> updateStatus(@PathVariable("id") long id, @PathVariable("status") String status) {
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
