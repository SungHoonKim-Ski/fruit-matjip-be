package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store.onuljang.config.KakaoConfigDto;
import store.onuljang.feign.dto.KakaoLoginResponse;
import store.onuljang.feign.KakaoAuthFeignClient;
import store.onuljang.feign.KakaoMeFeignClient;
import store.onuljang.feign.dto.KakaoMeRespone;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class KakaoService {
    KakaoAuthFeignClient authClient;
    KakaoMeFeignClient meClient;
    KakaoConfigDto kakaoConfigDto;

    public KakaoLoginResponse kakaoLogin(String code, String redirect) {
        return authClient.login(kakaoConfigDto.getGrantType(), kakaoConfigDto.getKakaoKey(), redirect, code);
    }

    public KakaoMeRespone getKakaoUserInfo(KakaoLoginResponse token) {
        return meClient.getUser(token.getBearerAccessToken(), kakaoConfigDto.getConetntType());
    }
}
