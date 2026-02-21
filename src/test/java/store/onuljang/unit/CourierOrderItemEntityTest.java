package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.shared.entity.enums.CourierOrderItemStatus;

class CourierOrderItemEntityTest {

    private CourierOrderItem createItem() {
        return CourierOrderItem.builder()
                .productName("감귤")
                .productPrice(new BigDecimal("15000"))
                .quantity(2)
                .amount(new BigDecimal("30000"))
                .build();
    }

    @Nested
    @DisplayName("markClaimRequested - 클레임 요청 상태 변경")
    class MarkClaimRequested {

        @Test
        @DisplayName("CLAIM_REQUESTED 상태로 변경된다")
        void markClaimRequested_success() {
            // arrange
            CourierOrderItem item = createItem();
            assertThat(item.getItemStatus()).isEqualTo(CourierOrderItemStatus.NORMAL);

            // act
            item.markClaimRequested();

            // assert
            assertThat(item.getItemStatus()).isEqualTo(CourierOrderItemStatus.CLAIM_REQUESTED);
        }
    }

    @Nested
    @DisplayName("markClaimResolved - 클레임 해결 상태 변경")
    class MarkClaimResolved {

        @Test
        @DisplayName("CLAIM_RESOLVED 상태로 변경된다")
        void markClaimResolved_success() {
            // arrange
            CourierOrderItem item = createItem();
            item.markClaimRequested();

            // act
            item.markClaimResolved();

            // assert
            assertThat(item.getItemStatus()).isEqualTo(CourierOrderItemStatus.CLAIM_RESOLVED);
        }
    }

    @Nested
    @DisplayName("markRefunded - 환불 완료 상태 변경")
    class MarkRefunded {

        @Test
        @DisplayName("REFUNDED 상태로 변경된다")
        void markRefunded_success() {
            // arrange
            CourierOrderItem item = createItem();
            item.markClaimRequested();

            // act
            item.markRefunded();

            // assert
            assertThat(item.getItemStatus()).isEqualTo(CourierOrderItemStatus.REFUNDED);
        }
    }
}
