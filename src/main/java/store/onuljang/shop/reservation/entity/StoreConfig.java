package store.onuljang.shop.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import store.onuljang.shared.entity.base.BaseEntity;

@Entity
@Table(name = "store_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoreConfig extends BaseEntity {

    @Column(name = "reservation_deadline_hour", nullable = false)
    int reservationDeadlineHour;

    @Column(name = "reservation_deadline_minute", nullable = false)
    int reservationDeadlineMinute;

    @Column(name = "cancellation_deadline_hour", nullable = false)
    int cancellationDeadlineHour;

    @Column(name = "cancellation_deadline_minute", nullable = false)
    int cancellationDeadlineMinute;

    @Column(name = "pickup_deadline_hour", nullable = false)
    int pickupDeadlineHour;

    @Column(name = "pickup_deadline_minute", nullable = false)
    int pickupDeadlineMinute;

    @Builder
    public StoreConfig(int reservationDeadlineHour, int reservationDeadlineMinute,
            int cancellationDeadlineHour, int cancellationDeadlineMinute,
            int pickupDeadlineHour, int pickupDeadlineMinute) {
        this.reservationDeadlineHour = reservationDeadlineHour;
        this.reservationDeadlineMinute = reservationDeadlineMinute;
        this.cancellationDeadlineHour = cancellationDeadlineHour;
        this.cancellationDeadlineMinute = cancellationDeadlineMinute;
        this.pickupDeadlineHour = pickupDeadlineHour;
        this.pickupDeadlineMinute = pickupDeadlineMinute;
    }

    public void update(int reservationDeadlineHour, int reservationDeadlineMinute,
            int cancellationDeadlineHour, int cancellationDeadlineMinute,
            int pickupDeadlineHour, int pickupDeadlineMinute) {
        this.reservationDeadlineHour = reservationDeadlineHour;
        this.reservationDeadlineMinute = reservationDeadlineMinute;
        this.cancellationDeadlineHour = cancellationDeadlineHour;
        this.cancellationDeadlineMinute = cancellationDeadlineMinute;
        this.pickupDeadlineHour = pickupDeadlineHour;
        this.pickupDeadlineMinute = pickupDeadlineMinute;
    }
}
