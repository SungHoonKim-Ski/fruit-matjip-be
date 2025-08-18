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
import store.onuljang.controller.request.PresignedUrlBatchRequest;
import store.onuljang.controller.request.PresignedUrlRequest;
import store.onuljang.controller.response.PresignedUrlResponse;
import store.onuljang.service.AdminUploadService;

import java.util.List;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Validated
public class AdminUploadController {

    AdminUploadService uploadService;

    @PostMapping("/products/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getTempUploadUrl(@Valid @RequestBody PresignedUrlRequest req) {
        PresignedUrlResponse res = uploadService.issueImageUrl(req.fileName(), req.contentType());
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/products/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getUploadUrl(@Valid @RequestBody PresignedUrlRequest req) {
        PresignedUrlResponse res = uploadService.issueImageUrl(req.fileName(), req.contentType());
        return ResponseEntity.ok(res);
    }

    // 상세 이미지 N개 교체/추가
    @PatchMapping("/products/{productId}/presigned-url")
    public ResponseEntity<List<PresignedUrlResponse>> getDetailUploadUrl(
            @Valid @Positive @NotNull @PathVariable Long productId,
            @Valid @RequestBody PresignedUrlBatchRequest req) {

        List<PresignedUrlResponse> res = uploadService.issueDetailImageUrls(productId, req.fileNames(), req.contentType());
        return ResponseEntity.ok(res);
    }
}