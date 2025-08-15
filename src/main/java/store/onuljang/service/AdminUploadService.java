package store.onuljang.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.stereotype.Service;
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
    Duration DEFAULT_EXPIRE = Duration.ofMinutes(10);
    ProductsService productsService;

    public void move(String srcKey, String destKey) {
        String bucket = s3Config.getBucket();
        s3.copyObject(bucket, srcKey, bucket, destKey);
        s3.deleteObject(bucket, srcKey);
    }

    public PresignedUrlResponse issueTempImageUrl(String filename, String contentType) {
        String ext = extOf(filename);
        String key = "images/temp/%s.%s".formatted(UUID.randomUUID(), ext);
        return presignPut(key, contentType, DEFAULT_EXPIRE);
    }

    public List<PresignedUrlResponse> issueDetailImageUrls(long productId, List<String> filenames, String contentType) {
        productsService.findById(productId); // validate

        List<PresignedUrlResponse> list = new ArrayList<>();

        for (int i = 0; i < filenames.size(); i++) {
            String ext = extOf(filenames.get(i));
            String key = "images/temp/%d/%s-%02d.%s".formatted(productId, UUID.randomUUID(), i + 1, ext);
            list.add(presignPut(key, contentType, DEFAULT_EXPIRE));
        }

        return list;
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

    public void softRemoveAll(long productId, List<String> removeKey) {
        String bucket = s3Config.getBucket();
        for (String key : removeKey) {
            s3.copyObject(bucket, key, bucket, "delete/%d/%s".formatted(productId,key));
        }

        DeleteObjectsRequest req = new DeleteObjectsRequest(bucket)
            .withQuiet(false)
            .withKeys(removeKey.toArray(new String[0]));

        s3.deleteObjects(req);
    }
}