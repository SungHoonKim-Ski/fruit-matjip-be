package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.exception.UserValidateException;
import store.onuljang.repository.entity.base.BaseEntity;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.*;

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
    private LocalDate pickupDate = LocalDate.now();

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

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
    private LocalDateTime statusChangedAt = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Reservation(Users user, Product product, Integer quantity, BigDecimal amount, LocalDate pickupDate) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.amount = amount;
        this.pickupDate = pickupDate;
    }

    public void cancelByUser(LocalDate today) {
        if (this.pickupDate.isBefore(today)) {
            throw new UserValidateException("과거 예약은 변경할 수 없습니다.");
        }
        if (this.status != ReservationStatus.PENDING && this.status != ReservationStatus.SELF_PICK) {
            throw new UserValidateException("취소할 수 없는 예약입니다.");
        }
        this.changeStatus(ReservationStatus.CANCELED);
    }

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
        statusChangedAt = LocalDateTime.now();
    }

    public String getReservationUserName() {
        return this.user.getName();
    }

    public String getReservationProductName() {
        return this.product.getName();
    }

    public String getReservationProductUrl() {
        return this.product.getProductUrl();
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}