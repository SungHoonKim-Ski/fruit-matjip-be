package store.onuljang.auth;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import store.onuljang.service.AdminUserDetailService;
import store.onuljang.service.dto.AdminUserDetails;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminAuthenticationProvider implements AuthenticationProvider {
    AdminUserDetailService adminUserDetailService;
    PasswordEncoder passwordEncoder;
    LoginAttemptService loginAttemptService;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        if (auth.getPrincipal() == null || auth.getCredentials() == null) {
            throw new BadCredentialsException("Missing credentials");
        }

        String email = String.valueOf(auth.getName()).trim().toLowerCase();
        String rawPw = String.valueOf(auth.getCredentials());

        if (loginAttemptService.isBlocked(email)) {
            throw new BadCredentialsException("계정이 일시적으로 잠겼습니다. 15분 후 다시 시도해주세요.");
        }

        AdminUserDetails user;
        try {
            user = adminUserDetailService.loadUserByUsername(email);
        } catch (AuthenticationException e) {
            loginAttemptService.loginFailed(email);
            throw new BadCredentialsException("Invalid admin credentials");
        }

        if (!passwordEncoder.matches(rawPw, user.getPassword())) {
            loginAttemptService.loginFailed(email);
            throw new BadCredentialsException("Invalid admin credentials");
        }

        loginAttemptService.loginSucceeded(email);
        return new AdminAuthenticationToken(user, null, user.getAuthorities(), user.getAdminId());
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(aClass);
    }
}
