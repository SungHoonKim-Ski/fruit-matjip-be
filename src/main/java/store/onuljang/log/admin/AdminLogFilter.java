package store.onuljang.log.admin;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import store.onuljang.auth.AdminAuthenticationToken;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminLogFilter extends OncePerRequestFilter {
    ApplicationEventPublisher publisher;
    AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (!matcher.match("/api/admin/**", uri)) return true;

        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String requestId = Optional.ofNullable(req.getHeader("X-Request-Id"))
                .orElseGet(() -> java.util.UUID.randomUUID().toString());

        res.setHeader("X-Request-Id", requestId);

        long start = System.nanoTime();
        int statusOnError = 500;

        try {
            chain.doFilter(req, res);
            long durationMs = (System.nanoTime() - start) / 1_000_000L;

            publisher.publishEvent(AdminLogEvent.builder()
                .adminId(currentAdminIdOrNull())
                .requestId(requestId)
                .status(statusOnError)
                .path(req.getRequestURI())
                .durationMs(durationMs)
                .requestId(requestId)
                .method(req.getMethod())
                .build()
            );
        } catch (Throwable t) {
            long durationMs = (System.nanoTime() - start) / 1_000_000L;
            publisher.publishEvent(AdminLogEvent.builder()
                .adminId(currentAdminIdOrNull())
                .requestId(requestId)
                .status(statusOnError)
                .path(req.getRequestURI())
                .durationMs(durationMs)
                .requestId(requestId)
                .method(req.getMethod())
                .build()
            );

            throw t;
        }
    }

    private Long currentAdminIdOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof AdminAuthenticationToken a) {
            return a.getAdminId();
        }
        return null;
    }
}