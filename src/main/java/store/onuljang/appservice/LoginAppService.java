package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import store.onuljang.controller.request.LoginRequest;
import store.onuljang.controller.response.LoginResponse;
import store.onuljang.feign.dto.KakaoLoginResponse;
import store.onuljang.feign.dto.KakaoMeRespone;
import store.onuljang.service.KakaoLoginService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginAppService {
    KakaoLoginService kakaoLoginService;
    public LoginResponse login(LoginRequest request) {
        KakaoLoginResponse token = kakaoLoginService.kakaoLogin(request.code(), request.redirectUri());
        KakaoMeRespone me = kakaoLoginService.getKakaoUserInfo(token);
        return LoginResponse.from(me);
    }
}
