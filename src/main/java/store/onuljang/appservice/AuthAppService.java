package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.component.JwtUtil;
import store.onuljang.controller.request.LoginRequest;
import store.onuljang.controller.response.LoginResponse;
import store.onuljang.feign.dto.KakaoLoginResponse;
import store.onuljang.feign.dto.KakaoMeRespone;
import store.onuljang.repository.entity.RefreshToken;
import store.onuljang.repository.entity.Users;
import store.onuljang.service.KakaoService;
import store.onuljang.service.TokenService;
import store.onuljang.service.UserService;
import store.onuljang.service.UserSignupService;
import store.onuljang.service.dto.JwtToken;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthAppService {
    KakaoService kakaoService;
    UserSignupService userSignupService;
    UserService userService;
    TokenService tokenService;
    JwtUtil jwtUtil;

    public LoginResponse socialLogin(LoginRequest request) {
        KakaoLoginResponse token = kakaoService.kakaoLogin(request.code(), request.redirectUri());
        KakaoMeRespone me = kakaoService.getKakaoUserInfo(token);

        Users user = userSignupService.ensureUserBySocialId(me.id());

        return afterSocialLogin(user);
    }

    public LoginResponse afterSocialLogin(Users user) {
        String accessToken = tokenService.generateToken(user);

        return LoginResponse
            .builder()
            .name(user.getName())
            .access(accessToken)
            .build();
    }


    @Transactional
    public String refresh(String accessBearerToken, String refreshToken) {
        String userUid = jwtUtil.getUid(accessBearerToken);
        RefreshToken curToken = tokenService.validate(userUid, refreshToken);

        Users user = userService.findByUId(userUid);
        return tokenService.generateToken(user, curToken);
    }
}
