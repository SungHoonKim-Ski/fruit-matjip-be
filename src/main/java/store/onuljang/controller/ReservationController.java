package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.ReservationAppService;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.controller.response.ReservationListResponse;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth/reservations")
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReservationController {
    ReservationAppService reservationAppService;

    @PostMapping("/")
    public ResponseEntity<Long> create(Authentication auth, @RequestBody @Valid ReservationRequest request) {
        String uid = auth.getName();

        return ResponseEntity.ok(reservationAppService.reserve(uid, request));
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<Void> cancel(Authentication auth,
            @Valid @PositiveOrZero @PathVariable("id") Long reservationId) {
        String uid = auth.getName();

        reservationAppService.cancel(uid, reservationId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<Void> removeMinusQuantity(Authentication auth,
            @Valid @PositiveOrZero @PathVariable("id") Long reservationId,
            @Valid @Positive @RequestParam Integer minus) {
        String uid = auth.getName();

        reservationAppService.minusQuantity(uid, reservationId, minus);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/self-pick/{id}")
    public ResponseEntity<Void> selfPick(Authentication auth,
            @Valid @PositiveOrZero @PathVariable("id") Long reservationId) {
        String uid = auth.getName();

        reservationAppService.selfPick(uid, reservationId);

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
