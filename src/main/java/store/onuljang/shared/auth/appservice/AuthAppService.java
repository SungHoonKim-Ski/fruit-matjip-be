package store.onuljang.shared.auth.appservice;

import store.onuljang.shared.auth.dto.LoginRequest;
import store.onuljang.shared.auth.dto.LoginResponse;

public interface AuthAppService {
    LoginResponse socialLogin(LoginRequest request);

    String refresh(String accessBearerToken, String refreshToken);
}
