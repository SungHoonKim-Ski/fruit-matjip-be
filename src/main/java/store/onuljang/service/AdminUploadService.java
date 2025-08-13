package store.onuljang.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.S3Config;
import store.onuljang.controller.response.PresignedUrlResponse;

import java.net.URL;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminUploadService {

    AmazonS3 s3;
    S3Config s3Config;
    static Duration DEFAULT_EXPIRE = Duration.ofMinutes(10);

    // 신규 상품(아직 productId 없음) 임시 업로드 URL
    @Transactional(readOnly = true)
    public PresignedUrlResponse issueTempImageUrl(Long adminId, String filename, String contentType) {
        String ext = extOf(filename);
        String key = "images/temp/%d/%s.%s".formatted(adminId, UUID.randomUUID(), ext);
        return presignPost(key, contentType, DEFAULT_EXPIRE);
    }

    // 대표 이미지 교체 URL
    @Transactional(readOnly = true)
    public PresignedUrlResponse issueMainImageUrl(Long productId, String filename, String contentType) {
        String ext = extOf(filename);
        String key = "images/images/products/%d/main/%s.%s".formatted(productId, UUID.randomUUID(), ext);
        return presignPut(key, contentType, DEFAULT_EXPIRE);
    }

    // 상세 이미지 N개 업로드 URL
    @Transactional(readOnly = true)
    public List<PresignedUrlResponse> issueDetailImageUrls(Long productId, List<String> filenames, String contentType) {
        List<PresignedUrlResponse> list = new ArrayList<>();
        int n = filenames.size();
        for (int i = 0; i < n; i++) {
            String ext = extOf(filenames.get(i));
            String key = "images/products/%d/details/%s-%02d.%s".formatted(productId, UUID.randomUUID(), i + 1, ext);
            list.add(presignPut(key, contentType, DEFAULT_EXPIRE));
        }
        return list;
    }

    private PresignedUrlResponse presignPost(String key, String contentType, Duration ttl) {
        Date expiration = new Date(System.currentTimeMillis() + ttl.toMillis());

        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(s3Config.getBucket(), key)
                .withMethod(HttpMethod.POST)
                .withExpiration(expiration);

        req.addRequestParameter("Content-Type", contentType);

        URL url = s3.generatePresignedUrl(req);

        return new PresignedUrlResponse(
                url.toExternalForm(),
                key,
                "POST",
                contentType,
                ttl.toSeconds()
        );
    }


    private PresignedUrlResponse presignPut(String key, String contentType, Duration ttl) {
        Date expiration = new Date(System.currentTimeMillis() + ttl.toMillis());

        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(s3Config.getBucket(), key)
            .withMethod(HttpMethod.PUT)
            .withExpiration(expiration);

        req.addRequestParameter("Content-Type", contentType);

        URL url = s3.generatePresignedUrl(req);

        return new PresignedUrlResponse(
            url.toExternalForm(),
            key,
            "PUT",
            contentType,
            ttl.toSeconds()
        );
    }

    private static String extOf(String filename) {
        int i = filename.lastIndexOf('.');
        String ext = (i >= 0 && i < filename.length() - 1) ? filename.substring(i + 1) : "bin";
        return ext.toLowerCase(Locale.ROOT);
    }
}