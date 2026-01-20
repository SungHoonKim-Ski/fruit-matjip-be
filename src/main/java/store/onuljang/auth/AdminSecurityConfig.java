package store.onuljang.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import store.onuljang.event.admin.AdminLogFilter;
import store.onuljang.service.dto.AdminUserDetails;

@Configuration
@RequiredArgsConstructor
@Order(1)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminSecurityConfig {
    AuthenticationProvider adminAuthProvider;
    AuthenticationManager authManager;
    ApplicationEventPublisher eventPublisher;

    @Bean
    @Order(1)
    SecurityFilterChain adminChain(HttpSecurity http, Validator validator) throws Exception {
        AdminLoginFilter loginFilter = getAdminLoginFilter(validator);

        http.securityMatcher("/api/admin/**")
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/admin/login", "/api/admin/signup", "/api/admin/logout").permitAll()
                .requestMatchers("/api/admin/**").hasAnyRole("MANAGER", "OWNER")
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authenticationProvider(adminAuthProvider)
                .exceptionHandling(e -> e
                    .authenticationEntryPoint((request, response, ex) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"ok\":false,\"message\":\"인증이 필요합니다.\"}");
                    })
                    .accessDeniedHandler((request, response, ex) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"ok\":false,\"message\":\"접근 권한이 없습니다.\"}");
                    })
                )
            .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(new AdminLogFilter(eventPublisher), SecurityContextHolderFilter.class)
            .cors(cors -> {});
        return http.build();
    }

    private AdminLoginFilter getAdminLoginFilter(Validator validator) {
        AdminLoginFilter loginFilter = new AdminLoginFilter(authManager, validator);

        loginFilter.setAuthenticationSuccessHandler((req, res, auth) -> {
            // 인증 정보 저장
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            HttpSession session = req.getSession(true); // 세션 없으면 생성
            new HttpSessionSecurityContextRepository().saveContext(context, req, res);

            AdminUserDetails principal = (AdminUserDetails) auth.getPrincipal();
            session.setAttribute("adminId", principal.getAdminId());

            // 응답
            res.setStatus(200);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"ok\":true}");
        });

        loginFilter.setAuthenticationFailureHandler((req, res, ex) -> {
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"ok\":false,\"message\":\"invalid credentials\"}");
        });
        return loginFilter;
    }
}
