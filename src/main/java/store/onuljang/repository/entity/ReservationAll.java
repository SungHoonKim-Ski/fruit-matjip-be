package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import store.onuljang.repository.entity.base.BaseEntity;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservations")
@SQLDelete(sql = "UPDATE reservations SET deleted_at = NOW() WHERE id = ?")
public class ReservationAll extends BaseEntity {

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
    public ReservationAll(Users user, Product product, Integer quantity, BigDecimal amount, LocalDate pickupDate) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.amount = amount;
        this.pickupDate = pickupDate;
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
