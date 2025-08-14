package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.ReservationAppService;
import store.onuljang.component.JwtUtil;
import store.onuljang.controller.request.ReservationListRequest;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.controller.response.ReservationListResponse;

@RestController
@RequestMapping("/api/auth/reservations")
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReservationController {
    JwtUtil jwtUtil;
    ReservationAppService reservationAppService;

    @PostMapping("/")
    public ResponseEntity<?> create(
            @RequestHeader(value="Authorization") String bearerToken,
            @RequestBody @Valid ReservationRequest request
    ) {
        String uId = jwtUtil.getBearerUid(bearerToken);

        return ResponseEntity.ok(reservationAppService.save(uId, request));
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<Void> cancel(
        @RequestHeader(value="Authorization") String bearerToken,
        @Valid @NotNull @PositiveOrZero @PathVariable("id") Long reservationId
    ) {
        String uId = jwtUtil.getBearerUid(bearerToken);

        reservationAppService.cancel(uId, reservationId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public ResponseEntity<ReservationListResponse> getList(
        @RequestHeader(value="Authorization") String bearerToken,
        @Valid @ModelAttribute @RequestBody ReservationListRequest request
    ) {
        String uId = jwtUtil.getBearerUid(bearerToken);

        return ResponseEntity.ok(reservationAppService.getReservations(uId, request));
    }
}


