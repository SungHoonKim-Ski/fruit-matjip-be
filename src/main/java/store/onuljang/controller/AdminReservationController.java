package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import store.onuljang.controller.response.AdminReservationListResponse;
import store.onuljang.controller.response.AdminReservationReportResponse;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.time.LocalDate;

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

    @PatchMapping("/{id}/warn")
    public ResponseEntity<Void> warnReservationUser(@PathVariable @Positive @NotNull Long id)
    {
        adminReservationAppService.warnReservationUser(id);

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
    public ResponseEntity<Integer> bulkUpdateReservationsStatus(
        @Valid @NotNull @RequestBody AdminUpdateReservationsRequest request)
    {
        return ResponseEntity.ok(adminReservationAppService.bulkUpdateReservationsStatus(request));
    }

    @GetMapping("/sails")
    public ResponseEntity<AdminReservationReportResponse> getSails(
       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate from,
       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate to)
    {
        return ResponseEntity.ok(adminReservationAppService.getSails(from, to));
    }
}


