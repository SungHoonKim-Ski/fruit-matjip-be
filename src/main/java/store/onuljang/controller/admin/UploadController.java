package store.onuljang.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UploadController {

    @PostMapping("/products/presigned-url")
    public ResponseEntity<?> getUploadUrl() {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/products/{productId}/presigned-url")
    public ResponseEntity<?> getUpdateUrl() {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/products/{productId}/detail/presigned-url")
    public ResponseEntity<?> getDetailUpdateUrl() {
        return ResponseEntity.ok().build();
    }
}