package store.onuljang.config.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@RequiredArgsConstructor
@Order(1)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminSecurityConfig {
    AuthenticationProvider adminAuthProvider;

    @Bean
    @Order(1)
    SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        AuthenticationSuccessHandler successHandler = getAuthenticationSuccessHandler();
        AuthenticationFailureHandler failureHandler = getAuthenticationFailureHandler();

        http
            .securityMatcher("/api/admin/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/login", "/api/admin/logout").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/admin/login", "/api/admin/logout"))
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authenticationProvider(adminAuthProvider)
            .formLogin(form -> form
                .loginPage("/api/admin/login")
                .loginProcessingUrl("/api/admin/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(lo -> lo
                .logoutUrl("/api/admin/logout")
                .logoutSuccessUrl("/api/admin/login?logout")
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .cors(cors -> {});
        return http.build();
    }

    private static AuthenticationFailureHandler getAuthenticationFailureHandler() {
        return (req, res, ex) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"ok\":false,\"message\":\"invalid credentials\"}");
        };
    }

    private static AuthenticationSuccessHandler getAuthenticationSuccessHandler() {
        AuthenticationSuccessHandler successHandler = (req, res, auth) -> {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"ok\":true}");
        };
        return successHandler;
    }
}