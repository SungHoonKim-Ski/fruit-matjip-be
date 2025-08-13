package store.onuljang.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
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
public class AdminUploadController {

    AdminUploadService uploadService;

    @PostMapping("/products/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getTempUploadUrl(@Valid @RequestBody PresignedUrlRequest req) {
        PresignedUrlResponse res = uploadService.issueTempImageUrl(req.adminId(), req.fileName(), req.contentType());
        return ResponseEntity.ok(res);
    }

    // 대표 이미지 교체
    @PatchMapping("/products/{productId}/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getMainUploadUrl(
            @PathVariable Long productId, @Valid @RequestBody PresignedUrlRequest req)
    {

        PresignedUrlResponse res = uploadService.issueMainImageUrl(productId, req.fileName(), req.contentType());
        return ResponseEntity.ok(res);
    }

    // 상세 이미지 N개 교체/추가
    @PatchMapping("/products/{productId}/detail/presigned-url")
    public ResponseEntity<List<PresignedUrlResponse>> getDetailUploadUrls(
            @PathVariable Long productId,
            @Valid @RequestBody PresignedUrlBatchRequest req) {

        if (!productId.equals(req.productId())) {
            return ResponseEntity.badRequest().build();
        }

        List<PresignedUrlResponse> res = uploadService.issueDetailImageUrls(productId, req.fileNames(), req.contentType());
        return ResponseEntity.ok(res);
    }
}