package store.onuljang.courier.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;
import store.onuljang.shared.entity.enums.CourierCompany;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.util.TimeUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "courier_orders")
public class CourierOrder extends BaseEntity {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_uid",
            referencedColumnName = "uid",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_courier_orders_user"))
    private Users user;

    @Getter
    @Column(name = "display_code", nullable = false, unique = true, length = 18)
    private String displayCode;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CourierOrderStatus status;

    @Getter
    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName;

    @Getter
    @Column(name = "receiver_phone", nullable = false, length = 30)
    private String receiverPhone;

    @Getter
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Getter
    @Column(name = "address1", nullable = false, length = 200)
    private String address1;

    @Getter
    @Column(name = "address2", length = 200)
    private String address2;

    @Getter
    @Column(name = "shipping_memo", length = 500)
    private String shippingMemo;

    @Getter
    @Column(name = "is_island", nullable = false)
    @Builder.Default
    private Boolean isIsland = false;

    @Getter
    @Column(name = "product_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal productAmount;

    @Getter
    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Getter
    @Column(name = "island_surcharge", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal islandSurcharge = BigDecimal.ZERO;

    @Getter
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Getter
    @Column(name = "point_used", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal pointUsed = BigDecimal.ZERO;

    @Getter
    @Column(name = "pg_tid", length = 100)
    private String pgTid;

    @Getter
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Getter
    @Column(name = "waybill_number", length = 50)
    private String waybillNumber;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "courier_company", length = 30)
    @Builder.Default
    private CourierCompany courierCompany = CourierCompany.LOGEN;

    @Getter
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Getter
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Getter
    @Column(name = "tracking_location", length = 200)
    private String trackingLocation;

    @Getter
    @Column(name = "tracking_updated_at")
    private LocalDateTime trackingUpdatedAt;

    @Getter
    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Getter
    @Version
    @Column(name = "version")
    private Long version;

    @Getter
    @Builder.Default
    @OneToMany(mappedBy = "courierOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourierOrderItem> items = new ArrayList<>();

    public void markPaid(String pgTid) {
        this.status = CourierOrderStatus.PAID;
        this.paidAt = TimeUtil.nowDateTime();
        this.pgTid = pgTid;
    }

    public void markOrdering() {
        this.status = CourierOrderStatus.ORDERING;
    }

    public void markOrderCompleted(String waybillNumber) {
        this.status = CourierOrderStatus.ORDER_COMPLETED;
        this.waybillNumber = waybillNumber;
        this.shippedAt = TimeUtil.nowDateTime();
    }

    public void markOrderCompleted(String waybillNumber, CourierCompany courierCompany) {
        this.status = CourierOrderStatus.ORDER_COMPLETED;
        this.waybillNumber = waybillNumber;
        this.courierCompany = courierCompany;
        this.shippedAt = TimeUtil.nowDateTime();
    }

    public void markInTransit() {
        this.status = CourierOrderStatus.IN_TRANSIT;
    }

    public void updateTrackingInfo(String location, LocalDateTime trackingTime) {
        this.trackingLocation = location;
        this.trackingUpdatedAt = trackingTime;
    }

    public void markDelivered() {
        this.status = CourierOrderStatus.DELIVERED;
        this.deliveredAt = TimeUtil.nowDateTime();
    }

    public void markCanceled() {
        this.status = CourierOrderStatus.CANCELED;
    }

    public void markFailed() {
        this.status = CourierOrderStatus.FAILED;
    }

    public boolean canMarkPaid() {
        return this.status == CourierOrderStatus.PENDING_PAYMENT;
    }

    public boolean canCancelByUser() {
        return this.status == CourierOrderStatus.PENDING_PAYMENT;
    }

    public boolean canFailByUser() {
        return this.status == CourierOrderStatus.PENDING_PAYMENT;
    }

    public void setPgTid(String pgTid) {
        this.pgTid = pgTid;
    }

    public void setPointUsed(BigDecimal pointUsed) {
        this.pointUsed = pointUsed;
    }

    public BigDecimal getPgPaymentAmount() {
        return this.totalAmount.subtract(this.pointUsed);
    }

    public void markPaidByPoint() {
        this.status = CourierOrderStatus.PAID;
        this.paidAt = TimeUtil.nowDateTime();
    }

    public String getProductSummary() {
        if (items == null || items.isEmpty()) {
            return "";
        }
        String firstName = items.get(0).getProductName();
        if (items.size() == 1) {
            return firstName;
        }
        return firstName + " 외 " + (items.size() - 1) + "건";
    }

    public int getTotalQuantity() {
        if (items == null) {
            return 0;
        }
        return items.stream().mapToInt(CourierOrderItem::getQuantity).sum();
    }
}
