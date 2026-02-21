package store.onuljang.courier.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;
import store.onuljang.shared.entity.enums.CourierPaymentStatus;
import store.onuljang.shared.entity.enums.PaymentProvider;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "courier_payments")
public class CourierPayment extends BaseEntity {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "courier_order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_courier_payments_order"))
    private CourierOrder courierOrder;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", nullable = false, length = 20)
    private PaymentProvider pgProvider;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CourierPaymentStatus status;

    @Getter
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Getter
    @Column(name = "tid", length = 100)
    private String tid;

    @Getter
    @Column(name = "aid", length = 100)
    private String aid;

    @Getter
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Getter
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Getter
    @Column(name = "canceled_amount", precision = 12, scale = 2)
    private BigDecimal canceledAmount;

    @Getter
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
}
