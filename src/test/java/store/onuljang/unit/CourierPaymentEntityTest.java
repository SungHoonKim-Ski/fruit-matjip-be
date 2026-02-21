package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.onuljang.courier.entity.CourierPayment;
import store.onuljang.shared.entity.enums.CourierPaymentStatus;
import store.onuljang.shared.entity.enums.PaymentProvider;

class CourierPaymentEntityTest {

    private CourierPayment createPayment(BigDecimal amount) {
        return CourierPayment.builder()
                .pgProvider(PaymentProvider.KAKAOPAY)
                .status(CourierPaymentStatus.READY)
                .amount(amount)
                .tid("T_TID_001")
                .build();
    }

    @Nested
    @DisplayName("markApproved - 결제 승인 처리")
    class MarkApproved {

        @Test
        @DisplayName("승인 시 상태, aid, approvedAt이 설정된다")
        void markApproved_success() {
            // arrange
            CourierPayment payment = createPayment(new BigDecimal("30000"));

            // act
            payment.markApproved("A_AID_001");

            // assert
            assertThat(payment.getStatus()).isEqualTo(CourierPaymentStatus.APPROVED);
            assertThat(payment.getAid()).isEqualTo("A_AID_001");
            assertThat(payment.getApprovedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("markCanceled - 전액 취소 처리")
    class MarkCanceled {

        @Test
        @DisplayName("취소 시 상태, canceledAt, canceledAmount가 설정된다")
        void markCanceled_success() {
            // arrange
            CourierPayment payment = createPayment(new BigDecimal("30000"));
            payment.markApproved("A_AID_001");

            // act
            payment.markCanceled();

            // assert
            assertThat(payment.getStatus()).isEqualTo(CourierPaymentStatus.CANCELED);
            assertThat(payment.getCanceledAt()).isNotNull();
            assertThat(payment.getCanceledAmount()).isEqualByComparingTo(new BigDecimal("30000"));
        }
    }

    @Nested
    @DisplayName("markPartialCanceled - 부분 취소 처리")
    class MarkPartialCanceled {

        @Test
        @DisplayName("부분 취소 시 상태, canceledAt, canceledAmount가 설정된다")
        void markPartialCanceled_success() {
            // arrange
            CourierPayment payment = createPayment(new BigDecimal("30000"));
            payment.markApproved("A_AID_001");

            // act
            payment.markPartialCanceled(new BigDecimal("15000"));

            // assert
            assertThat(payment.getStatus()).isEqualTo(CourierPaymentStatus.PARTIAL_CANCELED);
            assertThat(payment.getCanceledAt()).isNotNull();
            assertThat(payment.getCanceledAmount()).isEqualByComparingTo(new BigDecimal("15000"));
        }
    }

    @Nested
    @DisplayName("markFailed - 결제 실패 처리")
    class MarkFailed {

        @Test
        @DisplayName("실패 시 상태, failedAt이 설정된다")
        void markFailed_success() {
            // arrange
            CourierPayment payment = createPayment(new BigDecimal("30000"));

            // act
            payment.markFailed();

            // assert
            assertThat(payment.getStatus()).isEqualTo(CourierPaymentStatus.FAILED);
            assertThat(payment.getFailedAt()).isNotNull();
        }
    }
}
