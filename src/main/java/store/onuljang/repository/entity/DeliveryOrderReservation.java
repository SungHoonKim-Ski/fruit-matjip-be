package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.repository.entity.base.BaseEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "delivery_order_reservations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_delivery_order_reservation_reservation", columnNames = "reservation_id")
    },
    indexes = {
        @Index(name = "idx_delivery_order_reservations_order", columnList = "delivery_order_id")
    }
)
public class DeliveryOrderReservation extends BaseEntity {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "delivery_order_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_delivery_order_reservations_order")
    )
    private DeliveryOrder deliveryOrder;

    @Getter
    @JoinColumn(
        name = "reservation_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_delivery_order_reservations_reservation")
    )
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Reservation reservation;

    public void changeDeliveryOrder(DeliveryOrder deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
    }
}
