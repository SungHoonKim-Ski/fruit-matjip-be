package store.onuljang.shop.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.shop.reservation.appservice.ReservationAppService;
import store.onuljang.shop.reservation.dto.ReservationListResponse;
import store.onuljang.shop.reservation.dto.ReservationRequest;
import store.onuljang.shop.reservation.dto.StoreConfigResponse;
import store.onuljang.shop.reservation.service.StoreConfigService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/store/auth/reservations")
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReservationController {
    ReservationAppService reservationAppService;
    StoreConfigService storeConfigService;

    @GetMapping("/config")
    public ResponseEntity<StoreConfigResponse> getStoreConfig() {
        return ResponseEntity.ok(StoreConfigResponse.from(storeConfigService.getConfig()));
    }

    @PostMapping("/")
    public ResponseEntity<String> create(Authentication auth, @RequestBody @Valid ReservationRequest request) {
        String uid = auth.getName();

        return ResponseEntity.ok(reservationAppService.reserve(uid, request));
    }

    @PatchMapping("/cancel/{displayCode}")
    public ResponseEntity<Void> cancel(Authentication auth,
            @PathVariable("displayCode") String displayCode) {
        String uid = auth.getName();

        reservationAppService.cancel(uid, displayCode);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{displayCode}/quantity")
    public ResponseEntity<Void> removeMinusQuantity(Authentication auth,
            @PathVariable("displayCode") String displayCode,
            @Valid @Positive @RequestParam Integer minus) {
        String uid = auth.getName();

        reservationAppService.minusQuantity(uid, displayCode, minus);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public ResponseEntity<ReservationListResponse> getList(Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate to) {
        String uid = auth.getName();

        return ResponseEntity.ok(reservationAppService.getReservations(uid, from, to));
    }
}
