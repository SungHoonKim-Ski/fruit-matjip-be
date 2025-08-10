package store.onuljang.controller.request;

public record LoginRequest(
    String redirectUri,
    String code
) {

}
