package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.onuljang.courier.dto.ShippingFeeItemInput;
import store.onuljang.courier.dto.ShippingFeeResult;
import store.onuljang.courier.entity.CourierConfig;
import store.onuljang.courier.service.CourierConfigService;
import store.onuljang.courier.service.CourierProductService;
import store.onuljang.courier.service.CourierShippingFeeService;

@ExtendWith(MockitoExtension.class)
class CourierShippingFeeServiceTest {

    @InjectMocks private CourierShippingFeeService courierShippingFeeService;

    @Mock private CourierConfigService courierConfigService;

    @Mock private CourierProductService courierProductService;

    private CourierConfig createConfig(BigDecimal islandSurcharge) {
        return CourierConfig.builder().islandSurcharge(islandSurcharge).build();
    }

    @Nested
    @DisplayName("calculateByItems - 합배송 수량 기반 배송비 계산")
    class CalculateByItems {

        @Test
        @DisplayName("합배송 3개, 10개 주문, 배송비 3000원 → 12,000원")
        void combinedShipping3_quantity10_fee3000() {
            // Arrange
            when(courierConfigService.getConfig()).thenReturn(createConfig(new BigDecimal("3000")));
            List<ShippingFeeItemInput> items = List.of(
                    new ShippingFeeItemInput(1L, 10, BigDecimal.ZERO, new BigDecimal("3000"), 3));

            // Act
            ShippingFeeResult result = courierShippingFeeService.calculateByItems(items, "06000");

            // Assert — ceil(10/3) = 4, 4 × 3000 = 12,000
            assertThat(result.shippingFee()).isEqualByComparingTo(new BigDecimal("12000"));
            assertThat(result.isIsland()).isFalse();
            assertThat(result.islandSurcharge()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("합배송 1개(기본값), 3개 주문, 배송비 4000원 → 12,000원")
        void combinedShipping1_quantity3_fee4000() {
            // Arrange
            when(courierConfigService.getConfig()).thenReturn(createConfig(new BigDecimal("3000")));
            List<ShippingFeeItemInput> items = List.of(
                    new ShippingFeeItemInput(1L, 3, BigDecimal.ZERO, new BigDecimal("4000"), 1));

            // Act
            ShippingFeeResult result = courierShippingFeeService.calculateByItems(items, "06000");

            // Assert — ceil(3/1) = 3, 3 × 4000 = 12,000
            assertThat(result.shippingFee()).isEqualByComparingTo(new BigDecimal("12000"));
        }

        @Test
        @DisplayName("합배송 6개, 4개 주문, 배송비 6000원 → 6,000원")
        void combinedShipping6_quantity4_fee6000() {
            // Arrange
            when(courierConfigService.getConfig()).thenReturn(createConfig(new BigDecimal("3000")));
            List<ShippingFeeItemInput> items = List.of(
                    new ShippingFeeItemInput(1L, 4, BigDecimal.ZERO, new BigDecimal("6000"), 6));

            // Act
            ShippingFeeResult result = courierShippingFeeService.calculateByItems(items, "06000");

            // Assert — ceil(4/6) = 1, 1 × 6000 = 6,000
            assertThat(result.shippingFee()).isEqualByComparingTo(new BigDecimal("6000"));
        }

        @Test
        @DisplayName("정확히 나눠지는 경우: 합배송 3개, 6개 주문, 배송비 3000원 → 6,000원")
        void combinedShipping3_quantity6_exactDivision() {
            // Arrange
            when(courierConfigService.getConfig()).thenReturn(createConfig(new BigDecimal("3000")));
            List<ShippingFeeItemInput> items = List.of(
                    new ShippingFeeItemInput(1L, 6, BigDecimal.ZERO, new BigDecimal("3000"), 3));

            // Act
            ShippingFeeResult result = courierShippingFeeService.calculateByItems(items, "06000");

            // Assert — ceil(6/3) = 2, 2 × 3000 = 6,000
            assertThat(result.shippingFee()).isEqualByComparingTo(new BigDecimal("6000"));
        }

        @Test
        @DisplayName("수량 1개, 합배송 1개, 배송비 3000원 → 3,000원")
        void singleItem() {
            // Arrange
            when(courierConfigService.getConfig()).thenReturn(createConfig(new BigDecimal("3000")));
            List<ShippingFeeItemInput> items = List.of(
                    new ShippingFeeItemInput(1L, 1, BigDecimal.ZERO, new BigDecimal("3000"), 1));

            // Act
            ShippingFeeResult result = courierShippingFeeService.calculateByItems(items, "06000");

            // Assert — ceil(1/1) = 1, 1 × 3000 = 3,000
            assertThat(result.shippingFee()).isEqualByComparingTo(new BigDecimal("3000"));
        }

        @Test
        @DisplayName("여러 상품 합산: A(합배송3, 10개, 3000) + B(합배송1, 3개, 4000) → 24,000원")
        void multipleProducts() {
            // Arrange
            when(courierConfigService.getConfig()).thenReturn(createConfig(new BigDecimal("3000")));
            List<ShippingFeeItemInput> items = List.of(
                    new ShippingFeeItemInput(1L, 10, BigDecimal.ZERO, new BigDecimal("3000"), 3),
                    new ShippingFeeItemInput(2L, 3, BigDecimal.ZERO, new BigDecimal("4000"), 1));

            // Act
            ShippingFeeResult result = courierShippingFeeService.calculateByItems(items, "06000");

            // Assert — ceil(10/3)*3000 + ceil(3/1)*4000 = 12,000 + 12,000 = 24,000
            assertThat(result.shippingFee()).isEqualByComparingTo(new BigDecimal("24000"));
        }

        @Test
        @DisplayName("도서산간 주소 시 추가비 포함")
        void withIslandSurcharge() {
            // Arrange
            when(courierConfigService.getConfig()).thenReturn(createConfig(new BigDecimal("3000")));
            List<ShippingFeeItemInput> items = List.of(
                    new ShippingFeeItemInput(1L, 1, BigDecimal.ZERO, new BigDecimal("3000"), 1));

            // Act
            ShippingFeeResult result = courierShippingFeeService.calculateByItems(items, "63100");

            // Assert — 배송비 3,000 + 도서산간 3,000 = 6,000
            assertThat(result.shippingFee()).isEqualByComparingTo(new BigDecimal("3000"));
            assertThat(result.isIsland()).isTrue();
            assertThat(result.islandSurcharge()).isEqualByComparingTo(new BigDecimal("3000"));
            assertThat(result.totalShippingFee()).isEqualByComparingTo(new BigDecimal("6000"));
        }
    }

    @Nested
    @DisplayName("isIslandPostalCode - 도서산간 우편번호 판별")
    class IsIslandPostalCode {

        @Test
        @DisplayName("63000은 도서산간")
        void isIsland_63000_returnsTrue() {
            assertThat(courierShippingFeeService.isIslandPostalCode("63000")).isTrue();
        }

        @Test
        @DisplayName("63644는 도서산간")
        void isIsland_63644_returnsTrue() {
            assertThat(courierShippingFeeService.isIslandPostalCode("63644")).isTrue();
        }

        @Test
        @DisplayName("63320(중간값)은 도서산간")
        void isIsland_middle_returnsTrue() {
            assertThat(courierShippingFeeService.isIslandPostalCode("63320")).isTrue();
        }

        @Test
        @DisplayName("62999는 도서산간 아님")
        void isIsland_62999_returnsFalse() {
            assertThat(courierShippingFeeService.isIslandPostalCode("62999")).isFalse();
        }

        @Test
        @DisplayName("63645는 도서산간 아님")
        void isIsland_63645_returnsFalse() {
            assertThat(courierShippingFeeService.isIslandPostalCode("63645")).isFalse();
        }

        @Test
        @DisplayName("null이면 false")
        void isIsland_null_returnsFalse() {
            assertThat(courierShippingFeeService.isIslandPostalCode(null)).isFalse();
        }

        @Test
        @DisplayName("빈 문자열이면 false")
        void isIsland_empty_returnsFalse() {
            assertThat(courierShippingFeeService.isIslandPostalCode("")).isFalse();
        }

        @Test
        @DisplayName("공백 문자열이면 false")
        void isIsland_blank_returnsFalse() {
            assertThat(courierShippingFeeService.isIslandPostalCode("   ")).isFalse();
        }

        @Test
        @DisplayName("숫자가 아닌 문자열이면 false")
        void isIsland_nonNumeric_returnsFalse() {
            assertThat(courierShippingFeeService.isIslandPostalCode("abc")).isFalse();
        }
    }
}
