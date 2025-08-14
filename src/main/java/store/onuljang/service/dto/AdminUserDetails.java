package store.onuljang.service.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import store.onuljang.repository.entity.Admin;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class AdminUserDetails implements UserDetails, Serializable {
    Long adminId;
    String email;
    String password;
    String username;
    Collection<? extends GrantedAuthority> authorities;

    public AdminUserDetails(Admin admin) {
        this.adminId = admin.getId();
        this.email = admin.getEmail();
        this.password = admin.getPassword();
        this.username = admin.getName();
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + admin.getRole().name())
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
