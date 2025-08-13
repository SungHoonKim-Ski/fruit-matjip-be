package store.onuljang.config.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class AdminAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final Long adminId;

    public AdminAuthenticationToken(Object principal, Object credentials, Long adminId) {
        super(principal, credentials);
        this.adminId = adminId;
    }

    public Long getAdminId() {
        return adminId;
    }
}