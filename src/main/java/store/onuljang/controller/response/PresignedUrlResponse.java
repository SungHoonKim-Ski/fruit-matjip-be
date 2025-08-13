package store.onuljang.controller.response;

public record PresignedUrlResponse(
    String url,
    String key,
    String method,
    String contentType,
    long expiresIn
) {

}
