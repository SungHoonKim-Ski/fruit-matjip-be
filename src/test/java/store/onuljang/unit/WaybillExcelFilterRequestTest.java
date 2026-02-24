package store.onuljang.unit;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.onuljang.courier.dto.WaybillExcelFilterRequest;
import store.onuljang.shared.exception.UserValidateException;

@DisplayName("WaybillExcelFilterRequest 단위 테스트")
class WaybillExcelFilterRequestTest {

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("시작일이 종료일보다 이전이고 365일 이내이면 예외가 발생하지 않는다")
        void 유효한_날짜_범위_예외없음() {
            // Arrange
            LocalDate start = LocalDate.of(2025, 1, 1);
            LocalDate end = LocalDate.of(2025, 6, 1);
            WaybillExcelFilterRequest request = new WaybillExcelFilterRequest(start, end, null, null);

            // Act & Assert
            assertThatNoException().isThrownBy(request::validate);
        }

        @Test
        @DisplayName("종료일이 시작일보다 이전이면 UserValidateException이 발생한다")
        void 종료일이_시작일보다_이전_예외발생() {
            // Arrange
            LocalDate start = LocalDate.of(2025, 6, 1);
            LocalDate end = LocalDate.of(2025, 1, 1);
            WaybillExcelFilterRequest request = new WaybillExcelFilterRequest(start, end, null, null);

            // Act & Assert
            assertThatThrownBy(request::validate)
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("종료일은 시작일 이후여야 합니다");
        }

        @Test
        @DisplayName("조회 기간이 365일을 초과하면 UserValidateException이 발생한다")
        void 날짜범위_365일_초과_예외발생() {
            // Arrange
            LocalDate start = LocalDate.of(2025, 1, 1);
            LocalDate end = start.plusDays(366);
            WaybillExcelFilterRequest request = new WaybillExcelFilterRequest(start, end, null, null);

            // Act & Assert
            assertThatThrownBy(request::validate)
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("조회 기간은 최대 365일입니다");
        }

        @Test
        @DisplayName("productId가 null이어도 예외가 발생하지 않는다 (선택 필드)")
        void productId_null_예외없음() {
            // Arrange
            LocalDate start = LocalDate.of(2025, 3, 1);
            LocalDate end = LocalDate.of(2025, 3, 31);
            WaybillExcelFilterRequest request = new WaybillExcelFilterRequest(start, end, null, null);

            // Act & Assert
            assertThatNoException().isThrownBy(request::validate);
        }

        @Test
        @DisplayName("시작일과 종료일이 같으면 예외가 발생하지 않는다")
        void 시작일_종료일_같음_예외없음() {
            // Arrange
            LocalDate date = LocalDate.of(2025, 5, 15);
            WaybillExcelFilterRequest request = new WaybillExcelFilterRequest(date, date, 1L, null);

            // Act & Assert
            assertThatNoException().isThrownBy(request::validate);
        }
    }
}
