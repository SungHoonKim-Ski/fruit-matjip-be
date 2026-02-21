package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
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
import org.springframework.test.util.ReflectionTestUtils;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierPayment;
import store.onuljang.courier.service.CourierPaymentService;
import store.onuljang.courier.service.CourierRefundService;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.entity.enums.CourierPaymentStatus;
import store.onuljang.shared.entity.enums.PaymentProvider;
import store.onuljang.shared.feign.dto.request.KakaoPayCancelRequest;
import store.onuljang.shared.service.KakaoPayService;
import store.onuljang.shared.service.NaverPayService;
import store.onuljang.shared.service.TossPayService;
import store.onuljang.shared.user.entity.Users;

@ExtendWith(MockitoExtension.class)
class CourierRefundServiceTest {

    @InjectMocks private CourierRefundService courierRefundService;

    @Mock private KakaoPayService kakaoPayService;
    @Mock private NaverPayService naverPayService;
    @Mock private TossPayService tossPayService;
    @Mock private CourierPaymentService courierPaymentService;

    private Users testUser;

    @BeforeEach
    void setUp() {
        testUser =
                Users.builder()
                        .socialId("social123")
                        .name("테스트유저")
                        .uid(UUID.randomUUID())
                        .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    private CourierOrder createOrder(String pgTid) {
        CourierOrder order =
                CourierOrder.builder()
                        .user(testUser)
                        .displayCode("C-26021400-ABCD2")
                        .status(CourierOrderStatus.PAID)
                        .receiverName("홍길동")
                        .receiverPhone("010-1234-5678")
                        .postalCode("06134")
                        .address1("서울시 강남구")
                        .productAmount(BigDecimal.valueOf(30000))
                        .shippingFee(BigDecimal.valueOf(4000))
                        .totalAmount(BigDecimal.valueOf(34000))
                        .build();
        ReflectionTestUtils.setField(order, "id", 1L);
        ReflectionTestUtils.setField(order, "pgTid", pgTid);
        return order;
    }

    private CourierPayment createPayment(
            CourierOrder order, PaymentProvider provider, BigDecimal amount) {
        return CourierPayment.builder()
                .courierOrder(order)
                .pgProvider(provider)
                .status(CourierPaymentStatus.APPROVED)
                .amount(amount)
                .tid("T_TID_001")
                .build();
    }

    // --- PG별 환불 호출 ---

    @Nested
    @DisplayName("refund - PG별 환불 호출")
    class RefundByProvider {

        @Test
        @DisplayName("KAKAOPAY 환불 시 kakaoPayService.cancel 호출")
        void refund_kakaopay() {
            // arrange
            CourierOrder order = createOrder("T_PG_TID_001");
            CourierPayment payment =
                    createPayment(order, PaymentProvider.KAKAOPAY, new BigDecimal("34000"));

            given(courierPaymentService.findByCourierOrder(order))
                    .willReturn(Optional.of(payment));

            // act
            courierRefundService.refund(order, new BigDecimal("34000"));

            // assert
            verify(kakaoPayService).cancel(any(KakaoPayCancelRequest.class));
            verify(naverPayService, never()).cancel(any(), any(int.class), any());
            verify(tossPayService, never()).cancel(any(), any(int.class), any());
        }

        @Test
        @DisplayName("NAVERPAY 환불 시 naverPayService.cancel 호출")
        void refund_naverpay() {
            // arrange
            CourierOrder order = createOrder("T_PG_TID_001");
            CourierPayment payment =
                    createPayment(order, PaymentProvider.NAVERPAY, new BigDecimal("34000"));

            given(courierPaymentService.findByCourierOrder(order))
                    .willReturn(Optional.of(payment));

            // act
            courierRefundService.refund(order, new BigDecimal("34000"));

            // assert
            verify(naverPayService).cancel("T_PG_TID_001", 34000, "클레임 환불");
            verify(kakaoPayService, never()).cancel(any());
            verify(tossPayService, never()).cancel(any(), any(int.class), any());
        }

        @Test
        @DisplayName("TOSSPAY 환불 시 tossPayService.cancel 호출")
        void refund_tosspay() {
            // arrange
            CourierOrder order = createOrder("T_PG_TID_001");
            CourierPayment payment =
                    createPayment(order, PaymentProvider.TOSSPAY, new BigDecimal("34000"));

            given(courierPaymentService.findByCourierOrder(order))
                    .willReturn(Optional.of(payment));

            // act
            courierRefundService.refund(order, new BigDecimal("34000"));

            // assert
            verify(tossPayService).cancel("T_PG_TID_001", 34000, "클레임 환불");
            verify(kakaoPayService, never()).cancel(any());
            verify(naverPayService, never()).cancel(any(), any(int.class), any());
        }
    }

    // --- 전액/부분 취소 ---

    @Nested
    @DisplayName("refund - 전액/부분 취소 처리")
    class RefundAmountHandling {

        @Test
        @DisplayName("전액 환불 시 payment.markCanceled 호출")
        void refund_fullAmount_markCanceled() {
            // arrange
            CourierOrder order = createOrder("T_PG_TID_001");
            CourierPayment payment =
                    createPayment(order, PaymentProvider.KAKAOPAY, new BigDecimal("34000"));

            given(courierPaymentService.findByCourierOrder(order))
                    .willReturn(Optional.of(payment));

            // act
            courierRefundService.refund(order, new BigDecimal("34000"));

            // assert
            assertThat(payment.getStatus()).isEqualTo(CourierPaymentStatus.CANCELED);
            assertThat(payment.getCanceledAmount()).isEqualByComparingTo(new BigDecimal("34000"));
        }

        @Test
        @DisplayName("부분 환불 시 payment.markPartialCanceled 호출")
        void refund_partialAmount_markPartialCanceled() {
            // arrange
            CourierOrder order = createOrder("T_PG_TID_001");
            CourierPayment payment =
                    createPayment(order, PaymentProvider.KAKAOPAY, new BigDecimal("34000"));

            given(courierPaymentService.findByCourierOrder(order))
                    .willReturn(Optional.of(payment));

            // act
            courierRefundService.refund(order, new BigDecimal("15000"));

            // assert
            assertThat(payment.getStatus()).isEqualTo(CourierPaymentStatus.PARTIAL_CANCELED);
            assertThat(payment.getCanceledAmount()).isEqualByComparingTo(new BigDecimal("15000"));
        }
    }

    // --- 환불 스킵 조건 ---

    @Nested
    @DisplayName("refund - 환불 스킵 조건")
    class RefundSkip {

        @Test
        @DisplayName("결제 정보가 없으면 환불을 스킵한다")
        void refund_noPayment_skip() {
            // arrange
            CourierOrder order = createOrder("T_PG_TID_001");

            given(courierPaymentService.findByCourierOrder(order)).willReturn(Optional.empty());

            // act
            courierRefundService.refund(order, new BigDecimal("34000"));

            // assert
            verify(kakaoPayService, never()).cancel(any());
            verify(naverPayService, never()).cancel(any(), any(int.class), any());
            verify(tossPayService, never()).cancel(any(), any(int.class), any());
        }

        @Test
        @DisplayName("pgTid가 null이면 환불을 스킵한다")
        void refund_nullPgTid_skip() {
            // arrange
            CourierOrder order = createOrder(null);
            CourierPayment payment =
                    createPayment(order, PaymentProvider.KAKAOPAY, new BigDecimal("34000"));

            given(courierPaymentService.findByCourierOrder(order))
                    .willReturn(Optional.of(payment));

            // act
            courierRefundService.refund(order, new BigDecimal("34000"));

            // assert
            verify(kakaoPayService, never()).cancel(any());
        }
    }
}
