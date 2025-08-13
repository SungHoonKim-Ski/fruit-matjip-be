package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
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
public class ReservationController {
    JwtUtil jwtUtil;
    ReservationAppService reservationAppService;

    @PostMapping("/")
    public ResponseEntity<?> create(
            @RequestHeader(value="Authorization", required=false) String accessToken,
            @Valid ReservationRequest request
    ) {
        String uId = jwtUtil.getUid(accessToken);

        return ResponseEntity.ok(reservationAppService.save(uId, request));
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<Void> cancel(
        @RequestHeader(value="Authorization", required=false) String accessToken,
        @Valid @NotNull @PositiveOrZero @PathVariable("id") Long reservationId
    ) {
        String uId = jwtUtil.getUid(accessToken);

        reservationAppService.cancel(uId, reservationId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public ResponseEntity<ReservationListResponse> getList(
        @RequestHeader(value="Authorization", required=false) String accessToken,
        @Valid @ModelAttribute ReservationListRequest request
    ) {
        String uId = jwtUtil.getUid(accessToken);

        return ResponseEntity.ok(reservationAppService.getReservations(uId, request));
    }
}


