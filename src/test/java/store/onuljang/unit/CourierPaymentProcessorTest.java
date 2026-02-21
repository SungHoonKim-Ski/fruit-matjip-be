package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
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
import store.onuljang.courier.dto.CourierOrderReadyResponse;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierPayment;
import store.onuljang.courier.service.CourierPaymentProcessor;
import store.onuljang.courier.service.CourierPaymentService;
import store.onuljang.shared.config.KakaoPayConfigDto;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.entity.enums.PaymentProvider;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.feign.dto.reseponse.KakaoPayReadyResponse;
import store.onuljang.shared.service.KakaoPayService;
import store.onuljang.shared.service.NaverPayService;
import store.onuljang.shared.service.TossPayService;
import store.onuljang.shared.user.entity.Users;

@ExtendWith(MockitoExtension.class)
class CourierPaymentProcessorTest {

    @InjectMocks private CourierPaymentProcessor courierPaymentProcessor;

    @Mock private KakaoPayService kakaoPayService;
    @Mock private KakaoPayConfigDto kakaoPayConfigDto;
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

    private CourierOrder createOrder(String displayCode) {
        CourierOrder order =
                CourierOrder.builder()
                        .user(testUser)
                        .displayCode(displayCode)
                        .status(CourierOrderStatus.PENDING_PAYMENT)
                        .receiverName("홍길동")
                        .receiverPhone("010-1234-5678")
                        .postalCode("06134")
                        .address1("서울시 강남구")
                        .productAmount(BigDecimal.valueOf(30000))
                        .shippingFee(BigDecimal.valueOf(4000))
                        .totalAmount(BigDecimal.valueOf(34000))
                        .build();
        ReflectionTestUtils.setField(order, "id", 1L);
        return order;
    }

    @Nested
    @DisplayName("KakaoPay 결제 준비")
    class KakaoPayPrepare {

        @Test
        @DisplayName("카카오페이 활성화 시 KakaoPayService.ready 호출 후 redirect URL 반환")
        void preparePayment_kakaoPay_enabled() {
            // arrange
            CourierOrder order = createOrder("C-26021400-ABCD1");
            KakaoPayConfigDto.KakaoPayRedirectUrls redirectUrls =
                    new KakaoPayConfigDto.KakaoPayRedirectUrls(
                            "http://localhost/approve?order_id=C-26021400-ABCD1",
                            "http://localhost/cancel?order_id=C-26021400-ABCD1",
                            "http://localhost/fail?order_id=C-26021400-ABCD1");
            KakaoPayReadyResponse readyResponse =
                    new KakaoPayReadyResponse(
                            "T_TID_001",
                            "https://kakao.pay/redirect-pc",
                            "https://kakao.pay/redirect-mobile");

            given(kakaoPayConfigDto.isEnabled()).willReturn(true);
            given(kakaoPayConfigDto.buildCourierRedirectUrls("C-26021400-ABCD1"))
                    .willReturn(redirectUrls);
            given(kakaoPayService.ready(any())).willReturn(readyResponse);

            // act
            CourierOrderReadyResponse result =
                    courierPaymentProcessor.preparePayment(
                            order, testUser, PaymentProvider.KAKAOPAY);

            // assert
            assertThat(result.displayCode()).isEqualTo("C-26021400-ABCD1");
            assertThat(result.redirectUrl()).isEqualTo("https://kakao.pay/redirect-pc");
            assertThat(result.mobileRedirectUrl()).isEqualTo("https://kakao.pay/redirect-mobile");
            assertThat(order.getPgTid()).isEqualTo("T_TID_001");
            verify(courierPaymentService).save(any(CourierPayment.class));
        }

        @Test
        @DisplayName("카카오페이 비활성화 시 즉시 결제 처리")
        void preparePayment_kakaoPay_disabled_immediatePayment() {
            // arrange
            CourierOrder order = createOrder("C-26021400-ABCD1");

            given(kakaoPayConfigDto.isEnabled()).willReturn(false);

            // act
            CourierOrderReadyResponse result =
                    courierPaymentProcessor.preparePayment(
                            order, testUser, PaymentProvider.KAKAOPAY);

            // assert
            assertThat(result.displayCode()).isEqualTo("C-26021400-ABCD1");
            assertThat(result.redirectUrl()).isEqualTo("/shop/orders/C-26021400-ABCD1");
            verify(kakaoPayService, never()).ready(any());
        }

        @Test
        @DisplayName("provider 미지정 시 기본값 KAKAOPAY로 동작")
        void preparePayment_defaultProvider() {
            // arrange
            CourierOrder order = createOrder("C-26021400-ABCD1");

            given(kakaoPayConfigDto.isEnabled()).willReturn(false);

            // act
            CourierOrderReadyResponse result =
                    courierPaymentProcessor.preparePayment(order, testUser);

            // assert
            assertThat(result.displayCode()).isEqualTo("C-26021400-ABCD1");
            assertThat(result.redirectUrl()).isEqualTo("/shop/orders/C-26021400-ABCD1");
        }
    }

    @Nested
    @DisplayName("NaverPay 결제 준비")
    class NaverPayPrepare {

        @Test
        @DisplayName("네이버페이 비활성화 시 UserValidateException 발생")
        void preparePayment_naverPay_disabled() {
            // arrange
            CourierOrder order = createOrder("C-26021400-ABCD1");
            given(naverPayService.isEnabled()).willReturn(false);

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierPaymentProcessor.preparePayment(
                                            order, testUser, PaymentProvider.NAVERPAY))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("네이버페이는 현재 사용할 수 없습니다.");
        }

        @Test
        @DisplayName("네이버페이 활성화 시 연동 준비 중 예외 발생")
        void preparePayment_naverPay_enabled_notImplemented() {
            // arrange
            CourierOrder order = createOrder("C-26021400-ABCD1");
            given(naverPayService.isEnabled()).willReturn(true);

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierPaymentProcessor.preparePayment(
                                            order, testUser, PaymentProvider.NAVERPAY))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("네이버페이 연동이 준비 중입니다.");
            verify(courierPaymentService).save(any(CourierPayment.class));
        }
    }

    @Nested
    @DisplayName("TossPay 결제 준비")
    class TossPayPrepare {

        @Test
        @DisplayName("토스페이 비활성화 시 UserValidateException 발생")
        void preparePayment_tossPay_disabled() {
            // arrange
            CourierOrder order = createOrder("C-26021400-ABCD1");
            given(tossPayService.isEnabled()).willReturn(false);

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierPaymentProcessor.preparePayment(
                                            order, testUser, PaymentProvider.TOSSPAY))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("토스페이는 현재 사용할 수 없습니다.");
        }

        @Test
        @DisplayName("토스페이 활성화 시 연동 준비 중 예외 발생")
        void preparePayment_tossPay_enabled_notImplemented() {
            // arrange
            CourierOrder order = createOrder("C-26021400-ABCD1");
            given(tossPayService.isEnabled()).willReturn(true);

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierPaymentProcessor.preparePayment(
                                            order, testUser, PaymentProvider.TOSSPAY))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("토스페이 연동이 준비 중입니다.");
            verify(courierPaymentService).save(any(CourierPayment.class));
        }
    }

    @Nested
    @DisplayName("PaymentProvider 파싱")
    class PaymentProviderParsing {

        @Test
        @DisplayName("문자열에서 PaymentProvider enum 변환 성공")
        void valueOf_success() {
            // act / assert
            assertThat(PaymentProvider.valueOf("KAKAOPAY")).isEqualTo(PaymentProvider.KAKAOPAY);
            assertThat(PaymentProvider.valueOf("NAVERPAY")).isEqualTo(PaymentProvider.NAVERPAY);
            assertThat(PaymentProvider.valueOf("TOSSPAY")).isEqualTo(PaymentProvider.TOSSPAY);
        }

        @Test
        @DisplayName("잘못된 문자열로 PaymentProvider 변환 시 IllegalArgumentException 발생")
        void valueOf_invalid() {
            // act / assert
            assertThatThrownBy(() -> PaymentProvider.valueOf("INVALID_PAY"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
