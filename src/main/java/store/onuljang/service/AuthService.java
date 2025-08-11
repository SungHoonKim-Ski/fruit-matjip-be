package store.onuljang.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.onuljang.component.JwtUtil;
import store.onuljang.repository.entity.Users;
import store.onuljang.service.dto.JwtToken;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;

    public JwtToken generateToken(Users user) {
        String subject = user.getInternalUid();

        Map<String, Object> claims = Map.of(
                "typ", "access",
                "name",  user.getName()
        );


        String accessToken  = jwtUtil.generateAccessToken(subject, claims);
        String refreshToken = jwtUtil.generateRefreshToken(subject);

        return new JwtToken(accessToken, refreshToken);
    }
}