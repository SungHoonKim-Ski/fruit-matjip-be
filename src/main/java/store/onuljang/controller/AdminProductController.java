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
import store.onuljang.controller.request.AdminUpdateProductDetailsRequest;
import store.onuljang.controller.response.AdminProductDetailResponse;
import store.onuljang.controller.response.AdminProductListItems;
import store.onuljang.service.AdminProductAppService;

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
        return ResponseEntity.ok(adminProductAppService.saveAndMoveTempImage(request));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<?> updateDetail(
        @Valid @NotNull @Positive @PathVariable("productId") Long productId,
        @Valid @ModelAttribute AdminUpdateProductDetailsRequest request
    ) {
        adminProductAppService.updateDetail(productId, request);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/sold-out/{productId}")
    public ResponseEntity<?> setSoldOut(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.setSoldOut(productId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/visible/{productId}")
    public ResponseEntity<?> toggleVisible(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.toggleVisible(productId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> delete(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.delete(productId);

        return ResponseEntity.ok().build();
    }
}


