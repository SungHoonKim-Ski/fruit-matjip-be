package store.onuljang.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.TestS3Config;
import store.onuljang.config.TestSqsConfig;
import store.onuljang.shop.delivery.service.DeliveryFeeCalculator;
import store.onuljang.shared.exception.UserValidateException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestS3Config.class, TestSqsConfig.class})
@Transactional
class DeliveryFeeCalculatorTest {

    @Autowired
    DeliveryFeeCalculator deliveryFeeCalculator;

    // Test config: STORE_LAT=37.556504, STORE_LNG=126.8372613
    // MAX_DISTANCE_KM=3, FEE_DISTANCE_KM=1.5, FEE_NEAR=2900, FEE_PER_100M=50

    @Test
    @DisplayName("기준 거리 이내(1.5km)는 기본 배달비 적용")
    void calculate_nearDistance_appliesBaseFee() {
        // given - 매장에서 약 44m 거리
        double nearLat = 37.556904;
        double nearLng = 126.8372613;

        // when
        DeliveryFeeCalculator.FeeResult result = deliveryFeeCalculator.calculate(nearLat, nearLng);

        // then
        assertThat(result.deliveryFee()).isEqualByComparingTo(new BigDecimal("2900"));
        assertThat(result.distanceKm()).isLessThanOrEqualTo(new BigDecimal("1.500"));
    }

    @Test
    @DisplayName("기준 거리 초과~최대 거리 이내는 추가 배달비 적용")
    void calculate_farDistance_appliesExtraFee() {
        // given - 매장에서 약 2.8km 거리
        double farLat = 37.5319560847746;
        double farLng = 126.846611592059;

        // when
        DeliveryFeeCalculator.FeeResult result = deliveryFeeCalculator.calculate(farLat, farLng);

        // then
        assertThat(result.deliveryFee()).isGreaterThan(new BigDecimal("2900"));
        assertThat(result.distanceKm()).isGreaterThan(new BigDecimal("1.500"));
        assertThat(result.distanceKm()).isLessThanOrEqualTo(new BigDecimal("3.000"));
    }

    @Test
    @DisplayName("최대 거리 초과 시 예외 발생")
    void calculate_overMaxDistance_throwsException() {
        // given - 매장에서 3km 초과 거리
        double overLat = 37.586504;
        double overLng = 126.8372613;

        // when / then
        assertThatThrownBy(() -> deliveryFeeCalculator.calculate(overLat, overLng))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("배달 가능 거리");
    }

    @Test
    @DisplayName("매장 위치와 동일 좌표는 기본 배달비")
    void calculate_sameAsStore_appliesBaseFee() {
        // given
        double storeLat = 37.556504;
        double storeLng = 126.8372613;

        // when
        DeliveryFeeCalculator.FeeResult result = deliveryFeeCalculator.calculate(storeLat, storeLng);

        // then
        assertThat(result.deliveryFee()).isEqualByComparingTo(new BigDecimal("2900"));
        assertThat(result.distanceKm()).isEqualByComparingTo(new BigDecimal("0.000"));
    }

    @Test
    @DisplayName("추가 배달비는 100m 단위 올림으로 계산")
    void calculate_extraFee_ceilsPer100m() {
        // given - 매장에서 약 2.8km 거리
        double farLat = 37.5319560847746;
        double farLng = 126.846611592059;

        // when
        DeliveryFeeCalculator.FeeResult result = deliveryFeeCalculator.calculate(farLat, farLng);

        // then - 추가 배달비는 50원의 배수여야 함
        BigDecimal extraFee = result.deliveryFee().subtract(new BigDecimal("2900"));
        assertThat(extraFee.remainder(new BigDecimal("50"))).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
