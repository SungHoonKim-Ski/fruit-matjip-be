package store.onuljang.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.onuljang.shared.util.DisplayCodeGenerator;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisplayCodeGeneratorTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 2, 12, 14, 30);

    @Test
    @DisplayName("generate - R prefix로 올바른 포맷 생성 (YYMMHHMM-XXXXX)")
    void generate_reservationPrefix_returnsCorrectFormat() {
        // when
        String code = DisplayCodeGenerator.generate("R", FIXED_TIME);

        // then
        assertThat(code).matches("R-26021430-[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{5}");
    }

    @Test
    @DisplayName("generate - D prefix로 올바른 포맷 생성")
    void generate_deliveryPrefix_returnsCorrectFormat() {
        // given
        LocalDateTime time = LocalDateTime.of(2026, 9, 5, 9, 5);

        // when
        String code = DisplayCodeGenerator.generate("D", time);

        // then
        assertThat(code).matches("D-26090905-[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{5}");
    }

    @Test
    @DisplayName("generateFallback - 6자리 랜덤 문자열 생성")
    void generateFallback_returnsCorrectFormat() {
        // when
        String code = DisplayCodeGenerator.generateFallback("R", FIXED_TIME);

        // then
        assertThat(code).matches("R-26021430-[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{6}");
    }

    @Test
    @DisplayName("generate - 안전 문자셋만 사용")
    void generate_usesSafeCharsetOnly() {
        // given
        String safeChars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";

        // when
        String code = DisplayCodeGenerator.generate("R", LocalDateTime.of(2026, 1, 1, 12, 0));
        String randomPart = code.substring(code.lastIndexOf('-') + 1);

        // then
        for (char c : randomPart.toCharArray()) {
            assertThat(safeChars).contains(String.valueOf(c));
        }
    }

    @Test
    @DisplayName("generate - 여러 번 호출해도 유일한 코드 생성")
    void generate_multipleCallsProduceDifferentCodes() {
        // when
        String code1 = DisplayCodeGenerator.generate("R", FIXED_TIME);

        // then
        boolean foundDifferent = false;
        for (int i = 0; i < 10; i++) {
            if (!DisplayCodeGenerator.generate("R", FIXED_TIME).equals(code1)) {
                foundDifferent = true;
                break;
            }
        }
        assertThat(foundDifferent).isTrue();
    }

    @Test
    @DisplayName("generateUnique - 충돌 시 fallback으로 재생성")
    void generateUnique_collisionRetry() {
        // given - 처음 2번 충돌, 3번째 성공
        int[] callCount = {0};

        // when
        String code = DisplayCodeGenerator.generateUnique("R", FIXED_TIME, c -> {
            callCount[0]++;
            return callCount[0] <= 2;
        });

        // then
        assertThat(code).matches("R-26021430-[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{6}");
        assertThat(callCount[0]).isEqualTo(3);
    }

    @Test
    @DisplayName("generateUnique - 충돌 없으면 첫 번째 코드 반환")
    void generateUnique_noCollision() {
        // when
        String code = DisplayCodeGenerator.generateUnique("R", FIXED_TIME, c -> false);

        // then
        assertThat(code).matches("R-26021430-[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{5}");
    }

    @Test
    @DisplayName("resolveCode - displayCode 포맷은 그대로 반환")
    void resolveCode_validDisplayCode() {
        // when
        String resolved = DisplayCodeGenerator.resolveCode("R", "R-26021430-A3F8K");

        // then
        assertThat(resolved).isEqualTo("R-26021430-A3F8K");
    }

    @Test
    @DisplayName("resolveCode - backfill 포맷은 그대로 반환")
    void resolveCode_backfillFormat() {
        // when
        String resolved = DisplayCodeGenerator.resolveCode("R", "R-123");

        // then
        assertThat(resolved).isEqualTo("R-123");
    }

    @Test
    @DisplayName("resolveCode - 숫자만 입력 시 prefix 추가")
    void resolveCode_numericInput() {
        // when
        String resolved = DisplayCodeGenerator.resolveCode("D", "42");

        // then
        assertThat(resolved).isEqualTo("D-42");
    }

    @Test
    @DisplayName("resolveCode - 유효하지 않은 포맷은 예외")
    void resolveCode_invalidFormat() {
        // when & then
        assertThatThrownBy(() -> DisplayCodeGenerator.resolveCode("R", "invalid-code"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 코드 형식");
    }

    @Test
    @DisplayName("resolveCode - prefix 있지만 잘못된 포맷은 예외")
    void resolveCode_wrongFormatWithPrefix() {
        // when & then
        assertThatThrownBy(() -> DisplayCodeGenerator.resolveCode("R", "R-xyz!@#"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 코드 형식");
    }
}
