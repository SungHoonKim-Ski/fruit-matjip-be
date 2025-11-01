package store.onuljang.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.AdminAggregationAppService;
import store.onuljang.controller.response.AdminReservationDetailsResponse;
import store.onuljang.controller.response.AdminReservationSummaryResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/agg/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AdminAggregationController {
    AdminAggregationAppService adminAggregationAppService;

    @GetMapping("/summary")
    public ResponseEntity<AdminReservationSummaryResponse> getSummary(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @PastOrPresent LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @PastOrPresent LocalDate to
    )
    {
        return ResponseEntity.ok(adminAggregationAppService.getAggregationSummary(from, to));
    }

    @GetMapping("/sales")
    public ResponseEntity<AdminReservationDetailsResponse> getDetails(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @Past LocalDate date)
    {
        return ResponseEntity.ok(adminAggregationAppService.getDetail(date));
    }
}


