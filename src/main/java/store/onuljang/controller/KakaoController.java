package store.onuljang.controller;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.onuljang.feign.KakaoFeignClient;
import store.onuljang.feign.dto.KakaoRequest;

import java.util.HashMap;

@RequestMapping("/api/auth/kakao")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoController {

    private final KakaoFeignClient feignClient;
    private final Environment env;

    @PostMapping
    public ResponseEntity<String> getToken(@RequestBody HashMap<String, String> contents) {
        System.out.println(contents);
        String clientId = env.getProperty("kakao.client.id");

        val data = new KakaoRequest(
                "authorization_code"
                , clientId
                , "http://localhost:3000/login"
                ,contents.get("code"));

        String res = feignClient.getToken(data.grant_type(), data.client_id(), data.redirect_uri(), data.code());
        return ResponseEntity.ok(res);
    }
}
