package store.onuljang.appservice;

import store.onuljang.controller.request.LoginRequest;
import store.onuljang.controller.response.LoginResponse;

public interface AuthAppService {
    LoginResponse socialLogin(LoginRequest request);

    String refresh(String accessBearerToken, String refreshToken);
}
