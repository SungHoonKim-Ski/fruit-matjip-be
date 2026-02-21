package store.onuljang.shop.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;
import store.onuljang.shared.entity.enums.PaymentProvider;
import store.onuljang.shared.util.TimeUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "delivery_payments",
    indexes = {
        @Index(name = "idx_delivery_payments_order", columnList = "delivery_order_id"),
        @Index(name = "idx_delivery_payments_tid", columnList = "tid")
    }
)
public class DeliveryPayment extends BaseEntity {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "delivery_order_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_delivery_payments_order")
    )
    private DeliveryOrder deliveryOrder;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", nullable = false, length = 20)
    private PaymentProvider pgProvider;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryPaymentStatus status;

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
    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    public void markApproved(String aid) {
        this.status = DeliveryPaymentStatus.APPROVED;
        this.aid = aid;
        this.approvedAt = TimeUtil.nowDateTime();
    }

    public void markCanceled() {
        this.status = DeliveryPaymentStatus.CANCELED;
        this.canceledAt = TimeUtil.nowDateTime();
    }

    public void markFailed() {
        this.status = DeliveryPaymentStatus.FAILED;
        this.failedAt = TimeUtil.nowDateTime();
    }
}
