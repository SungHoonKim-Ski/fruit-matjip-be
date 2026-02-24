package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import store.onuljang.courier.entity.CourierClaim;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.repository.CourierClaimRepository;
import store.onuljang.courier.service.CourierClaimService;
import store.onuljang.shared.entity.enums.CourierClaimStatus;
import store.onuljang.shared.entity.enums.CourierClaimType;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.entity.enums.ShippingFeeBearer;
import store.onuljang.shared.exception.NotFoundException;
import store.onuljang.shared.user.entity.Users;

@ExtendWith(MockitoExtension.class)
class CourierClaimServiceTest {

    @InjectMocks private CourierClaimService courierClaimService;

    @Mock private CourierClaimRepository courierClaimRepository;

    private CourierOrder testOrder;

    @BeforeEach
    void setUp() {
        Users testUser =
                Users.builder()
                        .socialId("social123")
                        .name("테스트유저")
                        .uid(UUID.randomUUID())
                        .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testOrder =
                CourierOrder.builder()
                        .user(testUser)
                        .displayCode("C-26021400-ABCD2")
                        .status(CourierOrderStatus.DELIVERED)
                        .receiverName("홍길동")
                        .receiverPhone("010-1234-5678")
                        .postalCode("06134")
                        .address1("서울시 강남구")
                        .productAmount(BigDecimal.valueOf(30000))
                        .shippingFee(BigDecimal.valueOf(4000))
                        .totalAmount(BigDecimal.valueOf(34000))
                        .build();
        ReflectionTestUtils.setField(testOrder, "id", 1L);
    }

    private CourierClaim createClaim(CourierClaimStatus status) {
        CourierClaim claim =
                CourierClaim.builder()
                        .courierOrder(testOrder)
                        .claimType(CourierClaimType.QUALITY_ISSUE)
                        .claimStatus(status)
                        .reason("과일 상태 불량")
                        .returnShippingFeeBearer(ShippingFeeBearer.SELLER)
                        .build();
        ReflectionTestUtils.setField(claim, "id", 100L);
        return claim;
    }

    // --- save ---

    @Nested
    @DisplayName("save - 클레임 저장")
    class Save {

        @Test
        @DisplayName("repository에 위임하여 저장한다")
        void save_delegatesToRepository() {
            // arrange
            CourierClaim claim = createClaim(CourierClaimStatus.REQUESTED);
            given(courierClaimRepository.save(claim)).willReturn(claim);

            // act
            CourierClaim result = courierClaimService.save(claim);

            // assert
            assertThat(result).isEqualTo(claim);
            verify(courierClaimRepository).save(claim);
        }
    }

    // --- findById ---

    @Nested
    @DisplayName("findById - 클레임 조회")
    class FindById {

        @Test
        @DisplayName("존재하는 클레임을 반환한다")
        void findById_success() {
            // arrange
            CourierClaim claim = createClaim(CourierClaimStatus.REQUESTED);
            given(courierClaimRepository.findById(100L)).willReturn(Optional.of(claim));

            // act
            CourierClaim result = courierClaimService.findById(100L);

            // assert
            assertThat(result).isEqualTo(claim);
            assertThat(result.getId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("존재하지 않는 클레임 조회 시 NotFoundException 발생")
        void findById_notFound() {
            // arrange
            given(courierClaimRepository.findById(999L)).willReturn(Optional.empty());

            // act / assert
            assertThatThrownBy(() -> courierClaimService.findById(999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("존재하지 않는 클레임");
        }
    }

    // --- findByOrder ---

    @Nested
    @DisplayName("findByOrder - 주문별 클레임 목록 조회")
    class FindByOrder {

        @Test
        @DisplayName("주문에 대한 클레임 목록을 반환한다")
        void findByOrder_success() {
            // arrange
            CourierClaim claim1 = createClaim(CourierClaimStatus.REQUESTED);
            CourierClaim claim2 = createClaim(CourierClaimStatus.IN_REVIEW);
            ReflectionTestUtils.setField(claim2, "id", 101L);

            given(courierClaimRepository.findByCourierOrderOrderByIdDesc(testOrder))
                    .willReturn(List.of(claim1, claim2));

            // act
            List<CourierClaim> result = courierClaimService.findByOrder(testOrder);

            // assert
            assertThat(result).hasSize(2);
        }
    }

    // --- findAllByStatus ---

    @Nested
    @DisplayName("findAllByStatus - 상태별 클레임 목록 조회")
    class FindAllByStatus {

        @Test
        @DisplayName("상태 필터 + 페이지네이션으로 클레임 목록을 반환한다")
        void findAllByStatus_success() {
            // arrange
            CourierClaim claim = createClaim(CourierClaimStatus.REQUESTED);

            Page<CourierClaim> page =
                    new PageImpl<>(List.of(claim), PageRequest.of(0, 50), 1);
            given(courierClaimRepository.findAllByStatusOrderByIdDesc(
                            CourierClaimStatus.REQUESTED, PageRequest.of(0, 50)))
                    .willReturn(page);

            // act
            Page<CourierClaim> result =
                    courierClaimService.findAllByStatus(CourierClaimStatus.REQUESTED, 0, 50);

            // assert
            assertThat(result.getContent()).hasSize(1);
            verify(courierClaimRepository)
                    .findAllByStatusOrderByIdDesc(
                            CourierClaimStatus.REQUESTED, PageRequest.of(0, 50));
        }
    }
}
