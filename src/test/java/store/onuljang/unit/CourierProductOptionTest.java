package store.onuljang.unit;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.onuljang.courier.entity.CourierProductOption;
import store.onuljang.shared.exception.UserValidateException;

@DisplayName("CourierProductOption 단위 테스트")
class CourierProductOptionTest {

    private CourierProductOption optionWithStock(Integer stock) {
        return CourierProductOption.builder()
                .optionGroup(null)
                .name("테스트 옵션")
                .stock(stock)
                .build();
    }

    @Nested
    @DisplayName("assertPurchasable")
    class AssertPurchasable {

        @Test
        @DisplayName("재고가 충분하면 예외가 발생하지 않는다")
        void 재고_충분_예외없음() {
            // Arrange
            CourierProductOption option = optionWithStock(10);

            // Act & Assert
            assertThatNoException().isThrownBy(() -> option.assertPurchasable(5));
        }

        @Test
        @DisplayName("재고보다 많은 수량을 요청하면 UserValidateException이 발생한다")
        void 재고_부족_예외발생() {
            // Arrange
            CourierProductOption option = optionWithStock(3);

            // Act & Assert
            assertThatThrownBy(() -> option.assertPurchasable(5))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("재고가 부족합니다");
        }

        @Test
        @DisplayName("stock이 null이면 수량에 관계없이 예외가 발생하지 않는다 (무제한)")
        void 재고_null_무제한_예외없음() {
            // Arrange
            CourierProductOption option = optionWithStock(null);

            // Act & Assert
            assertThatNoException().isThrownBy(() -> option.assertPurchasable(9999));
        }
    }

    @Nested
    @DisplayName("purchase")
    class Purchase {

        @Test
        @DisplayName("재고가 있으면 구매 수량만큼 재고가 감소한다")
        void 구매_재고_감소() {
            // Arrange
            CourierProductOption option = optionWithStock(10);

            // Act
            option.purchase(3);

            // Assert
            assertThat(option.getStock()).isEqualTo(7);
        }

        @Test
        @DisplayName("stock이 null이면 구매 후에도 stock이 null로 유지된다")
        void 구매_재고_null_유지() {
            // Arrange
            CourierProductOption option = optionWithStock(null);

            // Act
            option.purchase(5);

            // Assert
            assertThat(option.getStock()).isNull();
        }

        @Test
        @DisplayName("재고보다 많은 수량을 구매하면 UserValidateException이 발생한다")
        void 구매_재고_부족_예외발생() {
            // Arrange
            CourierProductOption option = optionWithStock(2);

            // Act & Assert
            assertThatThrownBy(() -> option.purchase(5))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("재고가 부족합니다");
        }
    }

    @Nested
    @DisplayName("restoreStock")
    class RestoreStock {

        @Test
        @DisplayName("재고를 복구하면 재고가 복구 수량만큼 증가한다")
        void 재고_복구_증가() {
            // Arrange
            CourierProductOption option = optionWithStock(5);

            // Act
            option.restoreStock(3);

            // Assert
            assertThat(option.getStock()).isEqualTo(8);
        }

        @Test
        @DisplayName("stock이 null이면 재고 복구 후에도 null로 유지된다")
        void 재고_복구_null_유지() {
            // Arrange
            CourierProductOption option = optionWithStock(null);

            // Act
            option.restoreStock(3);

            // Assert
            assertThat(option.getStock()).isNull();
        }
    }
}
