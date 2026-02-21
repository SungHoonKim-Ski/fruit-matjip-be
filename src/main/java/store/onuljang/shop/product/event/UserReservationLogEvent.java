package store.onuljang.shop.product.event;

import lombok.Builder;
import store.onuljang.shared.entity.enums.UserProductAction;
import store.onuljang.shared.user.entity.log.UserReservationLog;

@Builder
public record UserReservationLogEvent(
    String userUid,
    Long reservationId,
    UserProductAction action
)
{
    public static UserReservationLog from(UserReservationLogEvent event) {
        return UserReservationLog.builder()
            .userUid(event.userUid())
            .reservationId(event.reservationId())
            .action(event.action())
            .build();
    }
}
