package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.onuljang.courier.dto.ShippingFeeResult;
import store.onuljang.courier.entity.CourierConfig;
import store.onuljang.courier.entity.ShippingFeePolicy;
import store.onuljang.courier.repository.ShippingFeePolicyRepository;
import store.onuljang.courier.service.CourierConfigService;
import store.onuljang.courier.service.CourierShippingFeeService;
import store.onuljang.shared.exception.UserValidateException;

@ExtendWith(MockitoExtension.class)
class CourierShippingFeeServiceTest {

    @InjectMocks private CourierShippingFeeService courierShippingFeeService;

    @Mock private ShippingFeePolicyRepository shippingFeePolicyRepository;

    @Mock private CourierConfigService courierConfigService;

    private ShippingFeePolicy createPolicy(int minQty, int maxQty, BigDecimal fee) {
        return ShippingFeePolicy.builder()
                .minQuantity(minQty)
                .maxQuantity(maxQty)
                .fee(fee)
                .build();
    }

    @Nested
    @DisplayName("calculate - 배송비 계산")
    class Calculate {

        @Test
        @DisplayName("수량 1~3 범위는 4000원 배송비 적용")
        void calculate_smallQuantity_returnsBaseFee() {
            // arrange
            ShippingFeePolicy policy = createPolicy(1, 3, new BigDecimal("4000"));
            given(shippingFeePolicyRepository.findByQuantityRange(2)).willReturn(Optional.of(policy));

            // act
            ShippingFeeResult result = courierShippingFeeService.calculate(2, "06134");

            // assert
            assertThat(result.shippingFee()).isEqualByComparingTo(new BigDecimal("4000"));
            assertThat(result.isIsland()).isFalse();
            assertThat(result.islandSurcharge()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.totalShippingFee()).isEqualByComparingTo(new BigDecimal("4000"));
        }

        @Test
        @DisplayName("수량 4~6 범위는 8000원 배송비 적용")
        void calculate_largeQuantity_returnsHigherFee() {
            // arrange
            ShippingFeePolicy policy = createPolicy(4, 6, new BigDecimal("8000"));
            given(shippingFeePolicyRepository.findByQuantityRange(5)).willReturn(Optional.of(policy));

            // act
            ShippingFeeResult result = courierShippingFeeService.calculate(5, "06134");

            // assert
            assertThat(result.shippingFee()).isEqualByComparingTo(new BigDecimal("8000"));
            assertThat(result.isIsland()).isFalse();
            assertThat(result.totalShippingFee()).isEqualByComparingTo(new BigDecimal("8000"));
        }

        @Test
        @DisplayName("제주 우편번호(63000)는 도서산간 추가요금 적용")
        void calculate_jejuPostalCode_addsIslandSurcharge() {
            // arrange
            ShippingFeePolicy policy = createPolicy(1, 3, new BigDecimal("4000"));
            CourierConfig config =
                    CourierConfig.builder()
                            .islandSurcharge(new BigDecimal("3000"))
                            .build();

            given(shippingFeePolicyRepository.findByQuantityRange(1)).willReturn(Optional.of(policy));
            given(courierConfigService.getConfig()).willReturn(config);

            // act
            ShippingFeeResult result = courierShippingFeeService.calculate(1, "63000");

            // assert
            assertThat(result.isIsland()).isTrue();
            assertThat(result.shippingFee()).isEqualByComparingTo(new BigDecimal("4000"));
            assertThat(result.islandSurcharge()).isEqualByComparingTo(new BigDecimal("3000"));
            assertThat(result.totalShippingFee()).isEqualByComparingTo(new BigDecimal("7000"));
        }

        @Test
        @DisplayName("서울 우편번호(06134)는 도서산간 아님")
        void calculate_seoulPostalCode_noSurcharge() {
            // arrange
            ShippingFeePolicy policy = createPolicy(1, 3, new BigDecimal("4000"));
            given(shippingFeePolicyRepository.findByQuantityRange(1)).willReturn(Optional.of(policy));

            // act
            ShippingFeeResult result = courierShippingFeeService.calculate(1, "06134");

            // assert
            assertThat(result.isIsland()).isFalse();
            assertThat(result.islandSurcharge()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("수량에 맞는 배송비 정책이 없으면 UserValidateException")
        void calculate_noPolicyFound_throwsException() {
            // arrange
            given(shippingFeePolicyRepository.findByQuantityRange(100))
                    .willReturn(Optional.empty());

            // act / assert
            assertThatThrownBy(() -> courierShippingFeeService.calculate(100, "06134"))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("배송비 정책이 없습니다");
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

    @Nested
    @DisplayName("findAll - 전체 배송비 정책 조회")
    class FindAll {

        @Test
        @DisplayName("sortOrder 오름차순으로 전체 정책 반환")
        void findAll_returnsPoliciesInSortOrder() {
            // arrange
            ShippingFeePolicy p1 = createPolicy(1, 3, new BigDecimal("4000"));
            ShippingFeePolicy p2 = createPolicy(4, 6, new BigDecimal("8000"));
            given(shippingFeePolicyRepository.findAllByOrderBySortOrderAsc())
                    .willReturn(List.of(p1, p2));

            // act
            List<ShippingFeePolicy> result = courierShippingFeeService.findAll();

            // assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getFee()).isEqualByComparingTo(new BigDecimal("4000"));
            assertThat(result.get(1).getFee()).isEqualByComparingTo(new BigDecimal("8000"));
        }

        @Test
        @DisplayName("정책이 없으면 빈 리스트 반환")
        void findAll_noPolicies_returnsEmptyList() {
            // arrange
            given(shippingFeePolicyRepository.findAllByOrderBySortOrderAsc())
                    .willReturn(List.of());

            // act
            List<ShippingFeePolicy> result = courierShippingFeeService.findAll();

            // assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("replaceAll - 배송비 정책 일괄 교체")
    class ReplaceAll {

        @Test
        @DisplayName("기존 정책 삭제 후 새 정책 저장")
        void replaceAll_deletesAllThenSavesNew() {
            // arrange
            ShippingFeePolicy newPolicy = createPolicy(1, 5, new BigDecimal("5000"));
            List<ShippingFeePolicy> newPolicies = List.of(newPolicy);
            given(shippingFeePolicyRepository.saveAll(anyList())).willReturn(newPolicies);

            // act
            List<ShippingFeePolicy> result = courierShippingFeeService.replaceAll(newPolicies);

            // assert
            InOrder order = inOrder(shippingFeePolicyRepository);
            order.verify(shippingFeePolicyRepository).deleteAllInBatch();
            order.verify(shippingFeePolicyRepository).saveAll(newPolicies);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFee()).isEqualByComparingTo(new BigDecimal("5000"));
        }

        @Test
        @DisplayName("빈 리스트로 교체하면 모두 삭제")
        void replaceAll_emptyList_deletesAll() {
            // arrange
            given(shippingFeePolicyRepository.saveAll(anyList())).willReturn(List.of());

            // act
            List<ShippingFeePolicy> result = courierShippingFeeService.replaceAll(List.of());

            // assert
            then(shippingFeePolicyRepository).should().deleteAllInBatch();
            then(shippingFeePolicyRepository).should().saveAll(List.of());
            assertThat(result).isEmpty();
        }
    }
}
