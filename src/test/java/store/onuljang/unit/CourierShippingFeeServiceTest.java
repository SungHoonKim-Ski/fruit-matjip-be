package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.onuljang.courier.repository.ShippingFeeTemplateRepository;
import store.onuljang.courier.service.CourierConfigService;
import store.onuljang.courier.service.CourierProductService;
import store.onuljang.courier.service.CourierShippingFeeService;

@ExtendWith(MockitoExtension.class)
class CourierShippingFeeServiceTest {

    @InjectMocks private CourierShippingFeeService courierShippingFeeService;

    @Mock private ShippingFeeTemplateRepository shippingFeeTemplateRepository;

    @Mock private CourierConfigService courierConfigService;

    @Mock private CourierProductService courierProductService;

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
