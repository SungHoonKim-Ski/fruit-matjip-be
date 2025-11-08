package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.AdminReservationAppService;
import store.onuljang.controller.request.AdminUpdateReservationsRequest;
import store.onuljang.controller.response.AdminReservationDetailsResponse;
import store.onuljang.controller.response.AdminReservationListResponse;
import store.onuljang.controller.response.AdminReservationSummaryResponse;
import store.onuljang.controller.response.AdminReservationsTodayResponse;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AdminReservationController {
    AdminReservationAppService adminReservationAppService;

    @GetMapping
    public ResponseEntity<AdminReservationListResponse> getReservationsByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate date)
    {
        return ResponseEntity.ok(adminReservationAppService.getAllByDate(date));
    }

    @PatchMapping("/{id}/no-show")
    public ResponseEntity<Void> handleNoShow(@PathVariable @Positive @NotNull Long id)
    {
        adminReservationAppService.handleNoShow(id);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/{status}")
    public ResponseEntity<Void> updateReservationStatus(
        @PathVariable @Positive @NotNull Long id,
        @PathVariable @NotNull ReservationStatus status)
    {
        adminReservationAppService.updateReservationStatus(id, status);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/status")
    public ResponseEntity<Long> bulkUpdateReservationsStatus(
        @Valid @NotNull @RequestBody AdminUpdateReservationsRequest request)
    {
        return ResponseEntity.ok(adminReservationAppService.bulkUpdateReservationsStatus(request));
    }

    @GetMapping("/sales/today")
    public ResponseEntity<AdminReservationsTodayResponse> getTodaySails()
    {
        return ResponseEntity.ok(adminReservationAppService.getTodaySales());
    }
}


