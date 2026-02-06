package store.onuljang.event.user_product;

import lombok.Builder;
import store.onuljang.repository.entity.enums.UserProductAction;
import store.onuljang.repository.entity.log.UserReservationLog;

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
