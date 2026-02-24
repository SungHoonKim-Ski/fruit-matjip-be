package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import store.onuljang.courier.appservice.CourierAdminConfigAppService;
import store.onuljang.courier.dto.CourierConfigAdminResponse;
import store.onuljang.courier.dto.CourierConfigUpdateRequest;
import store.onuljang.courier.entity.CourierConfig;
import store.onuljang.courier.service.CourierConfigService;

@ExtendWith(MockitoExtension.class)
class CourierAdminConfigAppServiceTest {

    @InjectMocks private CourierAdminConfigAppService courierAdminConfigAppService;

    @Mock private CourierConfigService courierConfigService;

    private CourierConfig buildConfig() {
        CourierConfig config =
                CourierConfig.builder()
                        .enabled(true)
                        .islandSurcharge(new BigDecimal("3000"))
                        .baseShippingFee(new BigDecimal("3000"))
                        .combinedShippingEnabled(false)
                        .combinedShippingMaxQuantity(1)
                        .noticeText("공지사항")
                        .senderName("과일맛집")
                        .senderPhone("010-0000-0000")
                        .senderPhone2("02-1234-5678")
                        .senderAddress("서울시 중구 명동길 1")
                        .senderDetailAddress("1층")
                        .build();
        ReflectionTestUtils.setField(config, "id", 1L);
        return config;
    }

    @Nested
    @DisplayName("getConfig - 설정 조회")
    class GetConfig {

        @Test
        @DisplayName("설정을 조회하여 응답 반환")
        void getConfig_returnsResponse() {
            // arrange
            CourierConfig config = buildConfig();
            given(courierConfigService.getConfig()).willReturn(config);

            // act
            CourierConfigAdminResponse result = courierAdminConfigAppService.getConfig();

            // assert
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.enabled()).isTrue();
            assertThat(result.islandSurcharge()).isEqualByComparingTo(new BigDecimal("3000"));
            assertThat(result.baseShippingFee()).isEqualByComparingTo(new BigDecimal("3000"));
            assertThat(result.senderName()).isEqualTo("과일맛집");
            assertThat(result.senderPhone()).isEqualTo("010-0000-0000");
            assertThat(result.senderPhone2()).isEqualTo("02-1234-5678");
            assertThat(result.senderAddress()).isEqualTo("서울시 중구 명동길 1");
            assertThat(result.senderDetailAddress()).isEqualTo("1층");
            assertThat(result.noticeText()).isEqualTo("공지사항");
        }
    }

    @Nested
    @DisplayName("updateConfig - 설정 수정")
    class UpdateConfig {

        @Test
        @DisplayName("전체 필드 수정 시 모든 값 반영")
        void updateConfig_allFields_updatesAll() {
            // arrange
            CourierConfig config = buildConfig();
            given(courierConfigService.getConfig()).willReturn(config);
            given(courierConfigService.updateConfig(config)).willReturn(config);

            CourierConfigUpdateRequest request =
                    new CourierConfigUpdateRequest(
                            false,
                            new BigDecimal("5000"),
                            new BigDecimal("2500"),
                            null,
                            null,
                            "변경된 공지",
                            "김철수",
                            "010-9999-8888",
                            null,
                            "부산시 해운대구",
                            "202호");

            // act
            CourierConfigAdminResponse result =
                    courierAdminConfigAppService.updateConfig(request);

            // assert
            assertThat(result.enabled()).isFalse();
            assertThat(result.islandSurcharge()).isEqualByComparingTo(new BigDecimal("5000"));
            assertThat(result.baseShippingFee()).isEqualByComparingTo(new BigDecimal("2500"));
            assertThat(result.noticeText()).isEqualTo("변경된 공지");
            assertThat(result.senderName()).isEqualTo("김철수");
            assertThat(result.senderPhone()).isEqualTo("010-9999-8888");
            assertThat(result.senderPhone2()).isEqualTo("02-1234-5678");
            assertThat(result.senderAddress()).isEqualTo("부산시 해운대구");
            assertThat(result.senderDetailAddress()).isEqualTo("202호");
            verify(courierConfigService).updateConfig(config);
        }

        @Test
        @DisplayName("일부 필드만 수정 시 나머지는 기존 값 유지")
        void updateConfig_partialFields_keepsExisting() {
            // arrange
            CourierConfig config = buildConfig();
            given(courierConfigService.getConfig()).willReturn(config);
            given(courierConfigService.updateConfig(config)).willReturn(config);

            CourierConfigUpdateRequest request =
                    new CourierConfigUpdateRequest(
                            null,
                            new BigDecimal("4000"),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);

            // act
            CourierConfigAdminResponse result =
                    courierAdminConfigAppService.updateConfig(request);

            // assert
            assertThat(result.enabled()).isTrue();
            assertThat(result.islandSurcharge()).isEqualByComparingTo(new BigDecimal("4000"));
            assertThat(result.baseShippingFee()).isEqualByComparingTo(new BigDecimal("3000"));
            assertThat(result.noticeText()).isEqualTo("공지사항");
            assertThat(result.senderName()).isEqualTo("과일맛집");
            assertThat(result.senderPhone()).isEqualTo("010-0000-0000");
        }

        @Test
        @DisplayName("baseShippingFee 수정 시 반영")
        void updateConfig_baseShippingFee_updates() {
            // arrange
            CourierConfig config = buildConfig();
            given(courierConfigService.getConfig()).willReturn(config);
            given(courierConfigService.updateConfig(config)).willReturn(config);

            CourierConfigUpdateRequest request =
                    new CourierConfigUpdateRequest(
                            null,
                            null,
                            new BigDecimal("4500"),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);

            // act
            CourierConfigAdminResponse result =
                    courierAdminConfigAppService.updateConfig(request);

            // assert
            assertThat(result.baseShippingFee()).isEqualByComparingTo(new BigDecimal("4500"));
            assertThat(result.islandSurcharge()).isEqualByComparingTo(new BigDecimal("3000"));
        }
    }
}
