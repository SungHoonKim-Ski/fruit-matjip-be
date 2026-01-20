package store.onuljang.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AdminAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final Long adminId;

    public AdminAuthenticationToken(Object principal, Object credentials,
            Collection<? extends GrantedAuthority> authorities, Long adminId) {

        super(principal, credentials, authorities);
        this.adminId = adminId;
    }

    public Long getAdminId() {
        return adminId;
    }
}
