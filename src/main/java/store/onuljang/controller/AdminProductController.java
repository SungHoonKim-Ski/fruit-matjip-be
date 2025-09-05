package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.controller.request.AdminCreateProductRequest;
import store.onuljang.controller.request.AdminProductBulkUpdateSellDateRequest;
import store.onuljang.controller.request.AdminUpdateProductDetailsRequest;
import store.onuljang.controller.response.AdminProductDetailResponse;
import store.onuljang.controller.response.AdminProductListItems;
import store.onuljang.appservice.AdminProductAppService;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AdminProductController {
    AdminProductAppService adminProductAppService;

    @GetMapping
    public ResponseEntity<AdminProductListItems> getAll() {
        return ResponseEntity.ok(adminProductAppService.getAll());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<AdminProductDetailResponse> getDetail(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        return ResponseEntity.ok(adminProductAppService.getDetail(productId));
    }

    @PostMapping
    public ResponseEntity<Long> create(@Valid @RequestBody AdminCreateProductRequest request) {
        return ResponseEntity.ok(adminProductAppService.save(request));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Void> updateDetail(
        @Valid @NotNull @Positive @PathVariable("productId") Long productId,
        @Valid @RequestBody AdminUpdateProductDetailsRequest request
    ) {
        adminProductAppService.updateDetail(productId, request);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/sold-out/{productId}")
    public ResponseEntity<Void> setSoldOut(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.setSoldOut(productId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/visible/{productId}")
    public ResponseEntity<Void> toggleVisible(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.toggleVisible(productId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/bulk-sell-date")
    public ResponseEntity<Integer> bulkUpdateSellDate(@Valid @RequestBody AdminProductBulkUpdateSellDateRequest request) {
        return ResponseEntity.ok(adminProductAppService.bulkUpdateSellDate(request));
    }
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.delete(productId);

        return ResponseEntity.ok().build();
    }
}


