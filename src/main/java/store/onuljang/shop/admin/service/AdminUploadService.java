package store.onuljang.shop.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shared.config.S3Config;
import store.onuljang.shop.admin.dto.PresignedUrlResponse;
import store.onuljang.shop.product.service.ProductsService;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class AdminUploadService {
    S3Config s3Config;
    S3Client s3;
    S3Presigner s3Presigner;
    ProductsService productsService;
    Duration DEFAULT_EXPIRE = Duration.ofMinutes(10);

    /** 단건 이미지 Presigned PUT URL 발급 */
    public PresignedUrlResponse issueImageUrl(String filename, String contentType) {
        String ext = extOf(filename);
        String key = "images/%s.%s".formatted(UUID.randomUUID(), ext);
        return presignPut(key, contentType, DEFAULT_EXPIRE);
    }

    /** 상세 이미지 다건 Presigned PUT URL 발급 */
    public List<PresignedUrlResponse> issueDetailImageUrls(long productId, List<String> filenames, String contentType) {
        productsService.findById(productId); // 존재 검증

        List<PresignedUrlResponse> list = new ArrayList<>(filenames.size());
        for (int i = 0; i < filenames.size(); i++) {
            String ext = extOf(filenames.get(i));
            String key = "images/%d/%s-%02d.%s".formatted(productId, UUID.randomUUID(), i + 1, ext);
            list.add(presignPut(key, contentType, DEFAULT_EXPIRE));
        }
        return list;
    }

    /** 소프트 삭제: images/ → images/delete/ 로 복사 후 원본 삭제 */
    public void softDeleteAllImages(List<String> removeKeys) {
        if (removeKeys == null || removeKeys.isEmpty())
            return;
        String bucket = s3Config.getBucket();

        for (String key : removeKeys) {
            String destKey = key.replaceFirst("^images/", "images/delete/");
            try {
                s3.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucket).sourceKey(key)
                .destinationBucket(bucket).destinationKey(destKey)
                .build());
            } catch (S3Exception | SdkClientException e) {
                log.error("S3 copy error (soft delete): {} -> {}, {}", key, destKey, e.getMessage(), e);
            }
        }

        try {
            List<ObjectIdentifier> ids = new ArrayList<>(removeKeys.size());
            for (String key : removeKeys) {
                ids.add(ObjectIdentifier.builder().key(key).build());
            }

            s3.deleteObjects(DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(ids).quiet(false).build())
                .build());
        } catch (S3Exception | SdkClientException e) {
            log.error("S3 deleteObjects error: {}", e.getMessage(), e);
        }
    }

    /** Presigned PUT (Content-Type 포함 서명) */
    private PresignedUrlResponse presignPut(String key, String contentType, Duration ttl) {
        String bucket = s3Config.getBucket();

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presign = PutObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .putObjectRequest(put)
                .build();

        PresignedPutObjectRequest result = s3Presigner.presignPutObject(presign);
        URL url = result.url();

        return new PresignedUrlResponse(
                url.toExternalForm(),
                key,
                "PUT",
                contentType,
                ttl.toSeconds()
        );
    }

    /* util */
    private static String extOf(String filename) {
        int i = (filename != null) ? filename.lastIndexOf('.') : -1;
        String ext = (i >= 0 && i < filename.length() - 1) ? filename.substring(i + 1) : "bin";
        return ext.toLowerCase(Locale.ROOT);
    }
}
