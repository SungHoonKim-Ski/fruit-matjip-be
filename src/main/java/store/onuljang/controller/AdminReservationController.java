package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
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
import store.onuljang.controller.response.AdminReservationListResponse;
import store.onuljang.controller.response.AdminReservationReportResponse;

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
        @Valid @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @FutureOrPresent @NotNull LocalDate date)
    {
        return ResponseEntity.ok(adminReservationAppService.getAllByDate(date));
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> togglePicked(@Valid @PathVariable @Positive @NotNull Long id) {
        adminReservationAppService.togglePicked(id);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/sails")
    public ResponseEntity<AdminReservationReportResponse> getSails(@Valid
       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate from,
       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate to)
    {
        return ResponseEntity.ok(adminReservationAppService.getSails(from, to));
    }
}


