package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.repository.entity.base.BaseEntity;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservations")
public class Reservation extends BaseEntity {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    private Users user;

    @Getter
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate = LocalDate.now();

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
    public Reservation(Users user, Product product, Integer quantity, BigDecimal amount) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.amount = amount;
    }

    public void changeStatus(ReservationStatus status) {
        this.status = status;
        statusChangedAt = LocalDateTime.now();
    }
}