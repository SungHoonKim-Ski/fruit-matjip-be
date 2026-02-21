package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
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
import store.onuljang.courier.dto.ShippingFeePolicyListResponse;
import store.onuljang.courier.dto.ShippingFeePolicyRequest;
import store.onuljang.courier.entity.CourierConfig;
import store.onuljang.courier.entity.ShippingFeePolicy;
import store.onuljang.courier.service.CourierConfigService;
import store.onuljang.courier.service.CourierShippingFeeService;

@ExtendWith(MockitoExtension.class)
class CourierAdminConfigAppServiceTest {

    @InjectMocks private CourierAdminConfigAppService courierAdminConfigAppService;

    @Mock private CourierConfigService courierConfigService;
    @Mock private CourierShippingFeeService courierShippingFeeService;

    private CourierConfig buildConfig() {
        CourierConfig config =
                CourierConfig.builder()
                        .enabled(true)
                        .islandSurcharge(new BigDecimal("3000"))
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

    private ShippingFeePolicy buildPolicy(
            Long id, int minQty, int maxQty, BigDecimal fee, int sortOrder) {
        ShippingFeePolicy policy =
                ShippingFeePolicy.builder()
                        .minQuantity(minQty)
                        .maxQuantity(maxQty)
                        .fee(fee)
                        .sortOrder(sortOrder)
                        .active(true)
                        .build();
        ReflectionTestUtils.setField(policy, "id", id);
        return policy;
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
                            null);

            // act
            CourierConfigAdminResponse result =
                    courierAdminConfigAppService.updateConfig(request);

            // assert
            assertThat(result.enabled()).isTrue();
            assertThat(result.islandSurcharge()).isEqualByComparingTo(new BigDecimal("4000"));
            assertThat(result.noticeText()).isEqualTo("공지사항");
            assertThat(result.senderName()).isEqualTo("과일맛집");
            assertThat(result.senderPhone()).isEqualTo("010-0000-0000");
        }
    }

    @Nested
    @DisplayName("getShippingFeePolicies - 배송비 정책 조회")
    class GetShippingFeePolicies {

        @Test
        @DisplayName("정책 목록을 응답으로 반환")
        void getShippingFeePolicies_returnsList() {
            // arrange
            ShippingFeePolicy p1 = buildPolicy(1L, 1, 3, new BigDecimal("4000"), 0);
            ShippingFeePolicy p2 = buildPolicy(2L, 4, 6, new BigDecimal("8000"), 1);
            given(courierShippingFeeService.findAll()).willReturn(List.of(p1, p2));

            // act
            ShippingFeePolicyListResponse result =
                    courierAdminConfigAppService.getShippingFeePolicies();

            // assert
            assertThat(result.policies()).hasSize(2);
            assertThat(result.policies().get(0).minQuantity()).isEqualTo(1);
            assertThat(result.policies().get(0).maxQuantity()).isEqualTo(3);
            assertThat(result.policies().get(0).fee()).isEqualByComparingTo(new BigDecimal("4000"));
            assertThat(result.policies().get(1).minQuantity()).isEqualTo(4);
            assertThat(result.policies().get(1).fee()).isEqualByComparingTo(new BigDecimal("8000"));
        }

        @Test
        @DisplayName("정책이 없으면 빈 리스트 반환")
        void getShippingFeePolicies_empty_returnsEmptyList() {
            // arrange
            given(courierShippingFeeService.findAll()).willReturn(List.of());

            // act
            ShippingFeePolicyListResponse result =
                    courierAdminConfigAppService.getShippingFeePolicies();

            // assert
            assertThat(result.policies()).isEmpty();
        }
    }

    @Nested
    @DisplayName("replaceShippingFeePolicies - 배송비 정책 일괄 교체")
    class ReplaceShippingFeePolicies {

        @Test
        @DisplayName("요청 목록을 엔티티로 변환하여 교체 후 응답 반환")
        void replaceShippingFeePolicies_success() {
            // arrange
            List<ShippingFeePolicyRequest> requests =
                    List.of(
                            new ShippingFeePolicyRequest(
                                    1, 3, new BigDecimal("4000"), 0, true),
                            new ShippingFeePolicyRequest(
                                    4, 6, new BigDecimal("8000"), 1, true));

            ShippingFeePolicy saved1 = buildPolicy(10L, 1, 3, new BigDecimal("4000"), 0);
            ShippingFeePolicy saved2 = buildPolicy(11L, 4, 6, new BigDecimal("8000"), 1);
            given(courierShippingFeeService.replaceAll(anyList()))
                    .willReturn(List.of(saved1, saved2));

            // act
            ShippingFeePolicyListResponse result =
                    courierAdminConfigAppService.replaceShippingFeePolicies(requests);

            // assert
            assertThat(result.policies()).hasSize(2);
            assertThat(result.policies().get(0).id()).isEqualTo(10L);
            assertThat(result.policies().get(0).fee())
                    .isEqualByComparingTo(new BigDecimal("4000"));
            assertThat(result.policies().get(1).id()).isEqualTo(11L);
            assertThat(result.policies().get(1).fee())
                    .isEqualByComparingTo(new BigDecimal("8000"));
            verify(courierShippingFeeService).replaceAll(anyList());
        }

        @Test
        @DisplayName("sortOrder와 active 기본값 처리")
        void replaceShippingFeePolicies_defaultValues() {
            // arrange
            List<ShippingFeePolicyRequest> requests =
                    List.of(
                            new ShippingFeePolicyRequest(
                                    1, 5, new BigDecimal("5000"), null, null));

            ShippingFeePolicy saved = buildPolicy(20L, 1, 5, new BigDecimal("5000"), 0);
            given(courierShippingFeeService.replaceAll(anyList()))
                    .willReturn(List.of(saved));

            // act
            ShippingFeePolicyListResponse result =
                    courierAdminConfigAppService.replaceShippingFeePolicies(requests);

            // assert
            assertThat(result.policies()).hasSize(1);
            assertThat(result.policies().get(0).sortOrder()).isEqualTo(0);
            assertThat(result.policies().get(0).active()).isTrue();
        }

        @Test
        @DisplayName("빈 리스트로 교체 시 빈 응답")
        void replaceShippingFeePolicies_emptyList() {
            // arrange
            given(courierShippingFeeService.replaceAll(anyList())).willReturn(List.of());

            // act
            ShippingFeePolicyListResponse result =
                    courierAdminConfigAppService.replaceShippingFeePolicies(List.of());

            // assert
            assertThat(result.policies()).isEmpty();
        }
    }
}
