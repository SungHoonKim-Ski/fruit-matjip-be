package store.onuljang.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    @PostMapping
    public ResponseEntity<?> create() {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<?> update(@PathVariable("productId") Long productId) {
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


