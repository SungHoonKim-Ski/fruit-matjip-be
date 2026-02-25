package store.onuljang.shared.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class AdminSessionIpFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.equals("/api/admin/login") || uri.equals("/api/admin/logout");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            String sessionIp = (String) session.getAttribute("sessionIp");
            if (sessionIp != null) {
                String currentIp = resolveClientIp(req);
                if (!sessionIp.equals(currentIp)) {
                    log.warn("[AdminSessionIpFilter] IP mismatch: session={}, current={}, adminId={}",
                        sessionIp, currentIp, session.getAttribute("adminId"));
                    session.invalidate();
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"ok\":false,\"message\":\"세션 IP가 변경되었습니다. 다시 로그인해주세요.\"}");
                    return;
                }
            }
        }
        chain.doFilter(req, res);
    }

    private String resolveClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
