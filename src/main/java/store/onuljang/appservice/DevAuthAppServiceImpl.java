package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.auth.JwtUtil;
import store.onuljang.controller.request.LoginRequest;
import store.onuljang.controller.response.LoginResponse;
import store.onuljang.repository.entity.RefreshToken;
import store.onuljang.repository.entity.Users;
import store.onuljang.service.TokenService;
import store.onuljang.service.UserService;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Profile("!PROD")
public class DevAuthAppServiceImpl implements AuthAppService {
    UserService userService;
    TokenService tokenService;
    JwtUtil jwtUtil;

    public LoginResponse socialLogin(LoginRequest request) {
        Users user = userService.findBySocialId("7777777");
        String accessToken = tokenService.generateToken(user);

        return LoginResponse
            .builder()
            .name(user.getName())
            .changeName(user.getChangeName())
            .access(accessToken)
            .build();
    }

    @Transactional
    public String refresh(String accessBearerToken, String refreshToken) {
        String userUid = jwtUtil.getUidFromExpiredToken(accessBearerToken);
        RefreshToken curToken = tokenService.validate(userUid, refreshToken);

        Users user = userService.findByUId(userUid);
        return tokenService.generateToken(user, curToken);
    }
}
