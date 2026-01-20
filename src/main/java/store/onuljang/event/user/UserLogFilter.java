package store.onuljang.event.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import store.onuljang.auth.JwtUtil;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserLogFilter extends OncePerRequestFilter {
    ApplicationEventPublisher eventPublisher;
    AntPathMatcher matcher = new AntPathMatcher();
    JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (!matcher.match("/api/auth/**", uri))
            return true;

        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method))
            return true;

        if (matcher.match("/api/login", uri) || matcher.match("/api/logout", uri) || matcher.match("/api/refresh", uri)
                || matcher.match("/api/health", uri)) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String requestId = Optional.ofNullable(req.getHeader("X-Request-Id"))
                .orElseGet(() -> java.util.UUID.randomUUID().toString());

        res.setHeader("X-Request-Id", requestId);

        long start = System.nanoTime();
        StatusCaptureResponseWrapper resp = new StatusCaptureResponseWrapper(res);

        try {
            chain.doFilter(req, resp);
        } catch (Throwable t) {
            if (resp.getStatus() < 400) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            throw t;
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000L;
            int status = resp.getStatus();

            eventPublisher.publishEvent(
                UserLogEvent.builder()
                    .userUid(currentUserIdOrNull(req))
                    .requestId(requestId)
                    .status(status)
                    .path(req.getRequestURI())
                    .durationMs(durationMs)
                    .method(req.getMethod())
                    .build()
            );
        }
    }
    @Nullable
    private String currentUserIdOrNull(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                return jwtUtil.getBearerUid(header);
            } catch (Exception ignore) {

            }
        }
        return null;
    }

    static class StatusCaptureResponseWrapper extends HttpServletResponseWrapper {
        private int httpStatus = SC_OK;

        StatusCaptureResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            this.httpStatus = sc;
        }
        @Override
        public void sendError(int sc) throws IOException {
            super.sendError(sc);
            this.httpStatus = sc;
        }
        @Override
        public void sendError(int sc, String msg) throws IOException {
            super.sendError(sc, msg);
            this.httpStatus = sc;
        }
        @Override
        public void sendRedirect(String location) throws IOException {
            super.sendRedirect(location);
            this.httpStatus = SC_FOUND;
        }
        @Override
        public int getStatus() {
            return this.httpStatus;
        }
    }
}
