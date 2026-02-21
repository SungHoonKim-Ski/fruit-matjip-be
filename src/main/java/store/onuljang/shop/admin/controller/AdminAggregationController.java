package store.onuljang.shop.admin.controller;

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
import store.onuljang.shop.admin.appservice.AdminAggregationAppService;
import store.onuljang.shop.admin.dto.AdminReservationDetailsResponse;
import store.onuljang.shop.admin.dto.AdminReservationSummaryResponse;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/shop/agg/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AdminAggregationController {
    AdminAggregationAppService adminAggregationAppService;

    @GetMapping("/summary")
    public ResponseEntity<AdminReservationSummaryResponse> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @PastOrPresent LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @PastOrPresent LocalDate to) {
        return ResponseEntity.ok(adminAggregationAppService.getAggregationSummary(from, to));
    }

    @GetMapping("/sales")
    public ResponseEntity<AdminReservationDetailsResponse> getDetails(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @Past LocalDate date) {
        return ResponseEntity.ok(adminAggregationAppService.getDetail(date));
    }
}
