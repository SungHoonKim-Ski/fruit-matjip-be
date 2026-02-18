package store.onuljang.auth;

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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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
            String typ = jws.getBody().get("typ", String.class);
            if (!"access".equals(typ)) {
                res.setStatus(HttpStatus.UNAUTHORIZED.value());
                res.getWriter().write("Invalid token type");
                return;
            }
            var auth = new UsernamePasswordAuthenticationToken(jws.getBody().getSubject(), null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(req, res);
        } catch (JwtException e) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401
            res.getWriter().write("Token expired");
        } catch (Exception ignore) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401
            res.getWriter().write("Invalid token");
        }
    }
}
