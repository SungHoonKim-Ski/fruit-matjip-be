package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.onuljang.courier.entity.CourierClaim;
import store.onuljang.shared.entity.enums.CourierClaimStatus;
import store.onuljang.shared.entity.enums.CourierClaimType;
import store.onuljang.shared.entity.enums.ShippingFeeBearer;

class CourierClaimEntityTest {

    private CourierClaim createClaim(CourierClaimStatus status) {
        return CourierClaim.builder()
                .claimType(CourierClaimType.QUALITY_ISSUE)
                .claimStatus(status)
                .reason("과일 상태 불량")
                .build();
    }

    @Nested
    @DisplayName("markInReview - 검토중 상태 변경")
    class MarkInReview {

        @Test
        @DisplayName("REQUESTED → IN_REVIEW 상태 변경")
        void markInReview_success() {
            // arrange
            CourierClaim claim = createClaim(CourierClaimStatus.REQUESTED);

            // act
            claim.markInReview();

            // assert
            assertThat(claim.getClaimStatus()).isEqualTo(CourierClaimStatus.IN_REVIEW);
        }
    }

    @Nested
    @DisplayName("approve - 클레임 승인")
    class Approve {

        @Test
        @DisplayName("승인 시 adminNote, refundAmount, bearer 설정")
        void approve_setsFields() {
            // arrange
            CourierClaim claim = createClaim(CourierClaimStatus.REQUESTED);

            // act
            claim.approve("승인합니다", BigDecimal.valueOf(15000), ShippingFeeBearer.SELLER);

            // assert
            assertThat(claim.getClaimStatus()).isEqualTo(CourierClaimStatus.APPROVED);
            assertThat(claim.getAdminNote()).isEqualTo("승인합니다");
            assertThat(claim.getRefundAmount()).isEqualByComparingTo(BigDecimal.valueOf(15000));
            assertThat(claim.getReturnShippingFeeBearer()).isEqualTo(ShippingFeeBearer.SELLER);
        }
    }

    @Nested
    @DisplayName("reject - 클레임 거부")
    class Reject {

        @Test
        @DisplayName("거부 시 REJECTED 상태 및 resolvedAt 설정")
        void reject_setsStatusAndResolvedAt() {
            // arrange
            CourierClaim claim = createClaim(CourierClaimStatus.REQUESTED);

            // act
            claim.reject("사유 부족으로 거부합니다");

            // assert
            assertThat(claim.getClaimStatus()).isEqualTo(CourierClaimStatus.REJECTED);
            assertThat(claim.getAdminNote()).isEqualTo("사유 부족으로 거부합니다");
            assertThat(claim.getResolvedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("resolve - 클레임 해결")
    class Resolve {

        @Test
        @DisplayName("해결 시 RESOLVED 상태 및 resolvedAt 설정")
        void resolve_setsStatusAndResolvedAt() {
            // arrange
            CourierClaim claim = createClaim(CourierClaimStatus.APPROVED);

            // act
            claim.resolve();

            // assert
            assertThat(claim.getClaimStatus()).isEqualTo(CourierClaimStatus.RESOLVED);
            assertThat(claim.getResolvedAt()).isNotNull();
        }
    }
}
