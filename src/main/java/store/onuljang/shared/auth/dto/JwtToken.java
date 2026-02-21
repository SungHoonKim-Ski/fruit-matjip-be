package store.onuljang.shared.auth.dto;

public record JwtToken(
    String access,
    String refresh
) {
}
