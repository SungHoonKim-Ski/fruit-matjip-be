package store.onuljang.shop.admin.dto;

public record PresignedUrlResponse(
    String url,
    String key,
    String method,
    String contentType,
    long expiresIn
) {

}
