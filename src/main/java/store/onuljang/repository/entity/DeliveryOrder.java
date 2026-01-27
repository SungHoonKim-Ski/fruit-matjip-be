package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.repository.entity.base.BaseEntity;
import store.onuljang.repository.entity.enums.DeliveryStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import store.onuljang.util.TimeUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "delivery_orders")
public class DeliveryOrder extends BaseEntity {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_uid",
        referencedColumnName = "uid",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_delivery_orders_user")
    )
    private Users user;

    @Getter
    @OneToMany(mappedBy = "deliveryOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryOrderReservation> deliveryOrderReservations = new ArrayList<>();

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryStatus status;

    @Getter
    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Getter
    @Column(name = "delivery_hour", nullable = false)
    private Integer deliveryHour;

    @Getter
    @Column(name = "delivery_minute", nullable = false)
    private Integer deliveryMinute;

    @Getter
    @Column(name = "delivery_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryFee;

    @Getter
    @Column(name = "distance_km", nullable = false, precision = 6, scale = 3)
    private BigDecimal distanceKm;

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
    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Getter
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Getter
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Getter
    @Column(name = "kakao_tid", length = 100)
    private String kakaoTid;

    @Getter
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public void setKakaoTid(String kakaoTid) {
        this.kakaoTid = kakaoTid;
    }

    public boolean canMarkPaid() {
        return this.status == DeliveryStatus.PENDING_PAYMENT;
    }

    public boolean canMarkOutForDelivery() {
        return this.status == DeliveryStatus.PAID;
    }

    public boolean canMarkDelivered() {
        return this.status == DeliveryStatus.OUT_FOR_DELIVERY;
    }

    public boolean canCancelByAdmin() {
        return this.status != DeliveryStatus.DELIVERED;
    }

    public boolean canCancelByUser() {
        return this.status == DeliveryStatus.PENDING_PAYMENT;
    }

    public boolean canFailByUser() {
        return this.status == DeliveryStatus.PENDING_PAYMENT;
    }

    public void markPaid() {
        this.status = DeliveryStatus.PAID;
        this.paidAt = TimeUtil.nowDateTime();
    }

    public void markOutForDelivery() {
        this.status = DeliveryStatus.OUT_FOR_DELIVERY;
    }

    public void markDelivered() {
        this.status = DeliveryStatus.DELIVERED;
    }

    public void markFailed() {
        this.status = DeliveryStatus.FAILED;
    }

    public void markCanceled() {
        this.status = DeliveryStatus.CANCELED;
    }

    public List<Reservation> getReservations() {
        return deliveryOrderReservations.stream()
            .map(DeliveryOrderReservation::getReservation)
            .toList();
    }

    public List<Long> getReservationIds() {
        return deliveryOrderReservations.stream()
            .map(link -> link.getReservation().getId())
            .toList();
    }

    public int getTotalQuantity() {
        return getReservations().stream()
            .mapToInt(Reservation::getQuantity)
            .sum();
    }

    public BigDecimal getTotalAmount() {
        return getReservations().stream()
            .map(Reservation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .add(getDeliveryFee());
    }

    public String getProductSummary() {
        return Reservation.buildSummary(getReservations());
    }
}
