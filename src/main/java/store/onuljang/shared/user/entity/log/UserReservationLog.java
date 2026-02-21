package store.onuljang.shared.user.entity.log;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import store.onuljang.shared.entity.base.BaseLogEntity;
import store.onuljang.shared.entity.enums.UserProductAction;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_reservation_logs")
@Builder
public class UserReservationLog extends BaseLogEntity {

    @Column(name = "user_uid", nullable = false, length = 36)
    private String userUid;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserProductAction action;

}
