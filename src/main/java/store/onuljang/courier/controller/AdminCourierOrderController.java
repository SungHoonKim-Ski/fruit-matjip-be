package store.onuljang.courier.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import store.onuljang.courier.appservice.CourierAdminOrderAppService;
import store.onuljang.courier.dto.AdminCourierOrderDetailResponse;
import store.onuljang.courier.dto.AdminCourierOrderListResponse;
import store.onuljang.courier.dto.CourierShipRequest;
import store.onuljang.courier.dto.CourierWaybillBulkRequest;
import store.onuljang.shared.entity.enums.CourierOrderStatus;

@RestController
@RequestMapping("/api/admin/courier/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AdminCourierOrderController {

    CourierAdminOrderAppService courierAdminOrderAppService;

    @GetMapping
    public ResponseEntity<AdminCourierOrderListResponse> getOrders(
            @RequestParam(required = false) CourierOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(courierAdminOrderAppService.getOrders(status, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminCourierOrderDetailResponse> getOrder(
            @Valid @NotNull @Positive @PathVariable("id") Long id) {
        return ResponseEntity.ok(courierAdminOrderAppService.getOrder(id));
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<Void> updateStatus(
            @Valid @NotNull @Positive @PathVariable("id") Long id,
            @PathVariable("status") CourierOrderStatus status) {
        courierAdminOrderAppService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/ship")
    public ResponseEntity<Void> ship(
            @Valid @NotNull @Positive @PathVariable("id") Long id,
            @Valid @RequestBody CourierShipRequest request) {
        courierAdminOrderAppService.ship(id, request.waybillNumber());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @Valid @NotNull @Positive @PathVariable("id") Long id) {
        courierAdminOrderAppService.cancel(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/waybill/excel")
    public ResponseEntity<byte[]> downloadWaybillExcel(
            @Valid @NotNull @Positive @PathVariable("id") Long id) {
        byte[] excelData = courierAdminOrderAppService.downloadWaybillExcel(id);
        String filename =
                "waybill-"
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + ".xlsx";
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    @PostMapping("/waybill/excel/bulk")
    public ResponseEntity<byte[]> downloadWaybillExcelBulk(
            @Valid @RequestBody CourierWaybillBulkRequest request) {
        byte[] excelData =
                courierAdminOrderAppService.downloadWaybillExcelBulk(request.orderIds());
        String filename =
                "waybill-bulk-"
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + ".xlsx";
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }
}
