package store.onuljang.shared.auth.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shared.auth.security.JwtUtil;
import store.onuljang.shared.auth.dto.LoginRequest;
import store.onuljang.shared.auth.dto.LoginResponse;
import store.onuljang.shared.auth.feign.dto.KakaoLoginResponse;
import store.onuljang.shared.auth.feign.dto.KakaoMeRespone;
import store.onuljang.shared.auth.entity.RefreshToken;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.auth.service.KakaoService;
import store.onuljang.shared.user.service.NameGenerator;
import store.onuljang.shared.auth.service.TokenService;
import store.onuljang.shared.user.service.UserService;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Profile("PROD")
public class ProdAuthAppServiceImpl implements AuthAppService {
    KakaoService kakaoService;
    UserService userService;
    TokenService tokenService;
    NameGenerator nameGenerator;
    JwtUtil jwtUtil;

    @Transactional
    public LoginResponse socialLogin(LoginRequest request) {
        KakaoLoginResponse token = kakaoService.kakaoLogin(request.code(), request.redirectUri());
        KakaoMeRespone me = kakaoService.getKakaoUserInfo(token);

        Users user = ensureUserBySocialId(me.id());

        String accessToken = tokenService.generateToken(user);

        return LoginResponse
            .builder()
            .name(user.getName())
            .changeName(user.getChangeName())
            .access(accessToken)
            .build();
    }

    @Transactional
    public Users ensureUserBySocialId(String socialId) {
        return userService.findOptionalBySocialId(socialId)
            .orElseGet(() -> {
                String uniqueName = nameGenerator.generate();
                Users newUser = Users.builder()
                    .name(uniqueName)
                    .socialId(socialId)
                    .uid(UUID.randomUUID())
                    .build();

                return userService.save(newUser);
            });
    }

    @Transactional
    public String refresh(String accessBearerToken, String refreshToken) {
        String userUid = jwtUtil.getUidFromExpiredToken(accessBearerToken);
        RefreshToken curToken = tokenService.validate(userUid, refreshToken);

        Users user = userService.findByUId(userUid);
        return tokenService.generateToken(user, curToken);
    }
}
