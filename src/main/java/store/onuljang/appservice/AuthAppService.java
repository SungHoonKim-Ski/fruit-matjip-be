package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import store.onuljang.controller.request.LoginRequest;
import store.onuljang.controller.response.LoginResponse;
import store.onuljang.feign.dto.KakaoLoginResponse;
import store.onuljang.feign.dto.KakaoMeRespone;
import store.onuljang.repository.entity.Users;
import store.onuljang.service.AuthService;
import store.onuljang.service.KakaoService;
import store.onuljang.service.UserSignupService;
import store.onuljang.service.dto.JwtToken;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthAppService {
    KakaoService kakaoService;
    UserSignupService userSignupService;
    AuthService authService;

    public LoginResponse socialLogin(LoginRequest request) {
        KakaoLoginResponse token = kakaoService.kakaoLogin(request.code(), request.redirectUri());
        KakaoMeRespone me = kakaoService.getKakaoUserInfo(token);

        Users user = userSignupService.ensureUserBySocialId(me.id());

        return afterSocialLogin(user);
    }

    public LoginResponse afterSocialLogin(Users user) {
        JwtToken jwtToken = authService.generateToken(user);

        return LoginResponse
            .builder()
            .name(user.getName())
            .access(jwtToken.access())
            .refresh(jwtToken.refresh())
            .build();
    }
}
