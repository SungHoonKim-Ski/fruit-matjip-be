package store.onuljang.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class KakaoConfigDto {
    final String grantType = "authorization_code";
    final String conetntType = "application/x-www-form-urlencoded;charset=utf-8";
    @Value("${KAKAO.KEY}")
    String kakaoKey;
    final String properties = "kakao_account.name";
}
