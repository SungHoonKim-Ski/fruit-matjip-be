package store.onuljang.courier.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;
import store.onuljang.shared.entity.enums.CourierClaimStatus;
import store.onuljang.shared.entity.enums.CourierClaimType;
import store.onuljang.shared.entity.enums.ShippingFeeBearer;
import store.onuljang.shared.util.TimeUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "courier_claims")
public class CourierClaim extends BaseEntity {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_order_id")
    private CourierOrder courierOrder;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_order_item_id")
    private CourierOrderItem courierOrderItem;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", nullable = false, length = 20)
    private CourierClaimType claimType;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "claim_status", nullable = false, length = 20)
    private CourierClaimStatus claimStatus;

    @Getter
    @Lob
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Getter
    @Lob
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Getter
    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Getter
    @Column(name = "reship_order_id")
    private Long reshipOrderId;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "return_shipping_fee_bearer", length = 20)
    private ShippingFeeBearer returnShippingFeeBearer;

    @Getter
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public void markInReview() {
        this.claimStatus = CourierClaimStatus.IN_REVIEW;
    }

    public void approve(String adminNote, BigDecimal refundAmount, ShippingFeeBearer bearer) {
        this.claimStatus = CourierClaimStatus.APPROVED;
        this.adminNote = adminNote;
        this.refundAmount = refundAmount;
        this.returnShippingFeeBearer = bearer;
    }

    public void reject(String adminNote) {
        this.claimStatus = CourierClaimStatus.REJECTED;
        this.adminNote = adminNote;
        this.resolvedAt = TimeUtil.nowDateTime();
    }

    public void resolve() {
        this.claimStatus = CourierClaimStatus.RESOLVED;
        this.resolvedAt = TimeUtil.nowDateTime();
    }

    public void setReshipOrderId(Long reshipOrderId) {
        this.reshipOrderId = reshipOrderId;
    }
}
