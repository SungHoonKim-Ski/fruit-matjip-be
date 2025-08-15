package store.onuljang.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import store.onuljang.controller.request.AdminLoginRequest;

import java.io.IOException;
import java.util.Set;

@Component
public class AdminLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Validator validator;

    public AdminLoginFilter(AuthenticationManager authenticationManager, Validator validator) {
        setAuthenticationManager(authenticationManager);
        setFilterProcessesUrl("/api/admin/login");
        this.validator = validator;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) {
        try (var is = req.getInputStream()) {
            AdminLoginRequest request = objectMapper.readValue(is, AdminLoginRequest.class);
            validate(request);

            UsernamePasswordAuthenticationToken auth = getAuthentication(request);

            setDetails(req, auth);

            return this.getAuthenticationManager().authenticate(auth);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Invalid login payload", e);
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(AdminLoginRequest request) {
        return new UsernamePasswordAuthenticationToken(request.email(), request.password());
    }

    private void validate(AdminLoginRequest request) {
        Set<ConstraintViolation<AdminLoginRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new AuthenticationServiceException("login validation failed");
        }
    }
}
