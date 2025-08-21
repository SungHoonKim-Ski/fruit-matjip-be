package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.ReservationAppService;
import store.onuljang.auth.JwtUtil;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.controller.response.ReservationListResponse;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth/reservations")
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReservationController {
    JwtUtil jwtUtil;
    ReservationAppService reservationAppService;

    @PostMapping("/")
    public ResponseEntity<Long> create(
        @RequestHeader(value="Authorization") String bearerToken,
        @RequestBody @Valid ReservationRequest request
    ) {
        String uId = jwtUtil.getBearerUid(bearerToken);

        return ResponseEntity.ok(reservationAppService.reserve(uId, request));
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<Void> cancel(
        @RequestHeader(value="Authorization") String bearerToken,
        @Valid @PositiveOrZero @PathVariable("id") Long reservationId
    ) {
        String uId = jwtUtil.getBearerUid(bearerToken);

        reservationAppService.cancel(uId, reservationId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/self-pick/{id}")
    public ResponseEntity<Void> selfPick(
            @RequestHeader(value="Authorization") String bearerToken,
            @Valid @PositiveOrZero @PathVariable("id") Long reservationId
    ) {
        String uId = jwtUtil.getBearerUid(bearerToken);

        reservationAppService.selfPick(uId, reservationId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public ResponseEntity<ReservationListResponse> getList(@Valid @RequestHeader(value="Authorization") String bearerToken,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate to
    ) {
        String uId = jwtUtil.getBearerUid(bearerToken);

        return ResponseEntity.ok(reservationAppService.getReservations(uId, from, to));
    }
}


