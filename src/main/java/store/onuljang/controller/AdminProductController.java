package store.onuljang.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.controller.request.AdminCreateProductRequest;
import store.onuljang.service.AdminProductAppService;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AdminProductController {
    AdminProductAppService adminProductAppService;

    @PostMapping
    public ResponseEntity<Long> create(@Valid AdminCreateProductRequest request) {
        return ResponseEntity.ok(adminProductAppService.saveAndMoveTempImage(request));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<?> updateDetail(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/sold-out/{productId}")
    public ResponseEntity<?> setSoldOut(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/visible/{productId}")
    public ResponseEntity<?> toggleVisible(@PathVariable("productId") Long productId, @RequestParam Boolean visible) {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> delete(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok().build();
    }
}


