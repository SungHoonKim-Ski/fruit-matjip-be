package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.exception.UserValidateException;
import store.onuljang.repository.entity.base.BaseEntity;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.util.TimeUtil;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservations")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE reservations SET deleted_at = NOW() WHERE id = ?")
public class Reservation extends BaseEntity {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_uid",
        referencedColumnName = "uid",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_res_user")
    )
    private Users user;

    @Getter
    @Column(name = "pickup_date", nullable = false)
    private LocalDate pickupDate = TimeUtil.nowDate();

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private ProductAll productAll;

    @Getter
    @OneToOne(mappedBy = "reservation", fetch = FetchType.LAZY)
    private DeliveryOrderReservation deliveryOrderReservation;

    @Getter
    @Setter
    @Column(name = "sellPrice", nullable = false)
    private BigDecimal sellPrice;

    @Getter
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Getter
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "status_changed_at", nullable = false)
    private LocalDateTime statusChangedAt = TimeUtil.nowDateTime();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Getter
    @Setter
    @Column(name = "display_code", nullable = false, length = 18)
    private String displayCode;

    @Builder
    public Reservation(Users user, Product product, Integer quantity, BigDecimal amount, BigDecimal sellPrice,
            LocalDate pickupDate, String displayCode) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.amount = amount;
        this.pickupDate = pickupDate;
        this.sellPrice = sellPrice;
        this.displayCode = displayCode;
        this.status = ReservationStatus.PENDING;
        this.statusChangedAt = TimeUtil.nowDateTime();
    }

    public void cancelByUser() {
        if (TimeUtil.isPastDate(pickupDate)) {
            throw new UserValidateException("과거 예약은 변경할 수 없습니다.");
        }
        if (TimeUtil.isCancelDeadlineOver(pickupDate)) {
            throw new UserValidateException("취소 가능 시각이 지났습니다.");
        }
        if (this.status != ReservationStatus.PENDING && this.status != ReservationStatus.SELF_PICK) {
            throw new UserValidateException("취소할 수 없는 예약입니다.");
        }

        this.changeStatus(ReservationStatus.CANCELED);
    }

    public void minusQuantityByUser(int minusQuantity) {
        if (TimeUtil.isPastDate(pickupDate)) {
            throw new UserValidateException("과거 예약은 변경할 수 없습니다.");
        }
        if (TimeUtil.isCancelDeadlineOver(pickupDate)) {
            throw new UserValidateException("취소 가능 시각이 지났습니다.");
        }
        if (this.status != ReservationStatus.PENDING) {
            throw new UserValidateException("변경할 수 없는 예약입니다.");
        }

        if (this.quantity - minusQuantity < 1) {
            throw new UserValidateException("변경 뒤 수량은 1개 이상이어야 합니다.");
        }

        this.minusQuantity(minusQuantity);
        this.amount = this.sellPrice.multiply(new BigDecimal(this.quantity));
    }

    @Deprecated
    public void requestSelfPick(LocalDate today, LocalTime deadline, ZoneId zone) {
        if (this.pickupDate.isBefore(today)) {
            throw new UserValidateException("과거 예약은 변경할 수 없습니다.");
        }
        if (this.status != ReservationStatus.PENDING) {
            throw new UserValidateException("셀프 수령이 불가능한 예약입니다.");
        }

        ZonedDateTime deadLine = this.pickupDate.atTime(deadline).atZone(zone);
        if (ZonedDateTime.now(zone).isAfter(deadLine)) {
            throw new UserValidateException("셀프 수령 신청이 마감됐습니다.");
        }
        this.changeStatus(ReservationStatus.SELF_PICK);
    }

    public void changeStatus(ReservationStatus status) {
        this.status = status;
        statusChangedAt = TimeUtil.nowDateTime();
    }

    public String getReservationUserName() {
        return this.user.getName();
    }

    public String getReservationProductName() {
        if (productAll != null && !Hibernate.isInitialized(product)) {
            return productAll.getName();
        }
        return this.product.getName();
    }

    public boolean getSelfPick() {
        if (productAll != null && !Hibernate.isInitialized(product)) {
            return Boolean.TRUE.equals(productAll.getSelfPick());
        }
        return this.product.getSelfPick();
    }

    public boolean getDeliveryAvailable() {
        if (productAll != null && !Hibernate.isInitialized(product)) {
            return Boolean.TRUE.equals(productAll.getDeliveryAvailable());
        }
        return this.product.getDeliveryAvailable();
    }

    public String getReservationProductUrl() {
        if (productAll != null && !Hibernate.isInitialized(product)) {
            return productAll.getProductUrl();
        }
        return this.product.getProductUrl();
    }

    public DeliveryOrder getDeliveryOrder() {
        if (deliveryOrderReservation == null) {
            return null;
        }
        return deliveryOrderReservation.getDeliveryOrder();
    }

    public static String buildSummary(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) return "배달 주문";
        String first = reservations.get(0).getReservationProductName();
        if (reservations.size() == 1) return first;
        return first + " 외 " + (reservations.size() - 1) + "건";
    }

    public static String buildFullSummary(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) return "배달 주문";
        return reservations.stream()
            .map(Reservation::getReservationProductName)
            .collect(java.util.stream.Collectors.joining(", "));
    }

    public boolean isNoShow() {
        return this.status == ReservationStatus.NO_SHOW;
    }

    public void noShow() {
        setStatus(ReservationStatus.NO_SHOW);
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    private void minusQuantity(int quantity) {
        this.quantity = Math.max(1, this.quantity - quantity);
    }

}
