package store.onuljang.controller.response;

import store.onuljang.feign.dto.KakaoMeRespone;

public record LoginResponse(
    Long id,
    String nickName
) {

    public static LoginResponse from(KakaoMeRespone respone) {
        return new LoginResponse(
            respone.id(),
            respone.properties().nickname()
        );
    }
}
