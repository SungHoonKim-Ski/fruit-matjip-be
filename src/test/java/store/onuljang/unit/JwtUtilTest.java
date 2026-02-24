package store.onuljang.unit;

import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import store.onuljang.shared.auth.security.JwtUtil;
import store.onuljang.config.TestS3Config;
import store.onuljang.config.TestSqsConfig;
import store.onuljang.shared.exception.AccessTokenParseException;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.auth.dto.JwtToken;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JwtUtil 단위 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Import({TestS3Config.class, TestSqsConfig.class})
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("generateToken - 토큰 생성")
    class GenerateToken {

        @Test
        @DisplayName("Users 객체로 토큰 생성 성공")
        void generateToken_FromUser_Success() {
            // given
            Users user = Users.builder().socialId("social-123").uid(UUID.randomUUID()).name("테스트유저").build();

            // when
            JwtToken token = jwtUtil.generateToken(user);

            // then
            assertThat(token.access()).isNotBlank();
            assertThat(token.refresh()).isNotBlank();
        }

        @Test
        @DisplayName("uid와 name으로 토큰 생성 성공")
        void generateToken_FromUidAndName_Success() {
            // given
            String uid = UUID.randomUUID().toString();
            String name = "테스트유저";

            // when
            JwtToken token = jwtUtil.generateToken(uid, name);

            // then
            assertThat(token.access()).isNotBlank();
            assertThat(token.refresh()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("getUid - UID 추출")
    class GetUid {

        @Test
        @DisplayName("유효한 토큰에서 UID 추출 성공")
        void getUid_ValidToken_Success() {
            // given
            String uid = UUID.randomUUID().toString();
            JwtToken token = jwtUtil.generateToken(uid, "테스트유저");

            // when
            String extractedUid = jwtUtil.getUid(token.access());

            // then
            assertThat(extractedUid).isEqualTo(uid);
        }
    }

    @Nested
    @DisplayName("parseAndValidate - 토큰 파싱 및 검증")
    class ParseAndValidate {

        @Test
        @DisplayName("유효한 토큰 파싱 성공")
        void parseAndValidate_ValidToken_Success() {
            // given
            String uid = UUID.randomUUID().toString();
            JwtToken token = jwtUtil.generateToken(uid, "테스트유저");

            // when
            Jws<Claims> claims = jwtUtil.parseAndValidate(token.access());

            // then
            assertThat(claims).isNotNull();
            assertThat(claims.getBody().getSubject()).isEqualTo(uid);
        }

        @Test
        @DisplayName("잘못된 토큰 파싱 시 예외 발생")
        void parseAndValidate_InvalidToken_ThrowsException() {
            // given
            String invalidToken = "invalid.token.here";

            // when & then
            assertThatThrownBy(() -> jwtUtil.parseAndValidate(invalidToken)).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("extractBearer - Bearer 토큰 추출")
    class ExtractBearer {

        @Test
        @DisplayName("Bearer 토큰 추출 성공")
        void extractBearer_ValidBearerToken_Success() {
            // given
            String uid = UUID.randomUUID().toString();
            JwtToken token = jwtUtil.generateToken(uid, "테스트유저");
            String bearerToken = "Bearer " + token.access();

            // when
            String extracted = jwtUtil.extractBearer(bearerToken);

            // then
            assertThat(extracted).isEqualTo(token.access());
        }

        @Test
        @DisplayName("Bearer 접두어 없는 토큰 시 예외 발생")
        void extractBearer_NoBearerPrefix_ThrowsException() {
            // given
            String tokenWithoutBearer = "some-token-without-bearer";

            // when & then
            assertThatThrownBy(() -> jwtUtil.extractBearer(tokenWithoutBearer))
                    .isInstanceOf(AccessTokenParseException.class);
        }

        @Test
        @DisplayName("null 토큰 시 예외 발생")
        void extractBearer_NullToken_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> jwtUtil.extractBearer(null)).isInstanceOf(AccessTokenParseException.class);
        }
    }

    @Nested
    @DisplayName("getBearerUid - Bearer 토큰에서 UID 추출")
    class GetBearerUid {

        @Test
        @DisplayName("Bearer 토큰에서 UID 추출 성공")
        void getBearerUid_Success() {
            // given
            String uid = UUID.randomUUID().toString();
            JwtToken token = jwtUtil.generateToken(uid, "테스트유저");
            String bearerToken = "Bearer " + token.access();

            // when
            String extractedUid = jwtUtil.getBearerUid(bearerToken);

            // then
            assertThat(extractedUid).isEqualTo(uid);
        }
    }
}
