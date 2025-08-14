package store.onuljang.config.auth;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import store.onuljang.component.JwtUtil;

import java.io.IOException;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class JwtFilter extends OncePerRequestFilter {
    JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = header.substring(7);
        try {
            var jws = jwtUtil.parseAndValidate(token);
            var auth = new UsernamePasswordAuthenticationToken(jws.getBody().getSubject(), null, java.util.List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(req, res);
        } catch (JwtException e) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());  // 401
            res.getWriter().write("Token expired");
        } catch (Exception ignore) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());  // 401
            res.getWriter().write("Invalid token");
        }
    }
}

