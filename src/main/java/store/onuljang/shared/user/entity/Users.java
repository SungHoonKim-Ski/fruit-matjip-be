package store.onuljang.shared.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.entity.base.BaseEntity;
import store.onuljang.shared.util.TimeUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
public class Users extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String socialId;

    @Getter
    @Column(nullable = false, length = 36)
    private String uid;

    @Getter
    @Column(nullable = false, length = 50)
    private String name;

    @Getter
    @Column
    private LocalDate lastOrderDate;

    @Column(nullable = false)
    private Long totalOrders = 0L;

    @Getter
    @Column(nullable = false)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Getter
    @Column(nullable = false)
    private Boolean changeName = false;

    @Column
    private LocalDateTime deletedAt;

    @Getter
    @Column(name = "monthly_warn_count")
    private Integer monthlyWarnCount = 0;

    @Getter
    @Column
    private Integer totalWarnCount = 0;

    @Getter
    @Column(name = "restricted_until")
    private LocalDate restrictedUntil;

    @Getter
    @Column(name = "point_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal pointBalance = BigDecimal.ZERO;

    @Builder
    public Users(String socialId, String name, UUID uid) {
        this.socialId = socialId;
        this.uid = uid.toString();
        this.name = name;
        this.totalRevenue = BigDecimal.ZERO;
        this.totalOrders = 0L;
        this.monthlyWarnCount = 0;
        this.totalWarnCount = 0;
        this.changeName = false;
        this.pointBalance = BigDecimal.ZERO;
    }

    public void addPointBalance(BigDecimal amount) {
        this.pointBalance = this.pointBalance.add(amount);
    }

    public void subtractPointBalance(BigDecimal amount) {
        if (this.pointBalance.compareTo(amount) < 0) {
            throw new UserValidateException("포인트 잔액이 부족합니다.");
        }
        this.pointBalance = this.pointBalance.subtract(amount);
    }

    public void modifyName(String name) {
        this.name = name;
        changeName = true;
    }

    public void addTotalOrders(long add) {
        this.totalOrders = Math.max(0, this.totalOrders + add);
    }

    public void removeTotalOrders(long remove) {
        this.totalOrders = Math.max(0, this.totalOrders - remove);
    }

    public void addTotalRevenue(BigDecimal add) {
        if (add == null)
            return;
        this.totalRevenue = this.totalRevenue.add(add).max(BigDecimal.ZERO);
    }

    public void removeTotalRevenue(BigDecimal remove) {
        if (remove == null)
            return;

        this.totalRevenue = this.totalRevenue.subtract(remove).max(BigDecimal.ZERO);
    }

    public void reserve(int quantity, BigDecimal amount, LocalDate today) {
        assertNicknameChanged();
        lastOrderDate = today;

        addTotalOrders(quantity);
        addTotalRevenue(amount);
    }

    public void cancelReserve(long quantity, BigDecimal amount) {
        removeTotalOrders(quantity);
        removeTotalRevenue(amount);
    }

    public boolean isRestricted() {
        return restrictedUntil != null && !TimeUtil.nowDate().isAfter(restrictedUntil);
    }

    public void restrict(LocalDate until) {
        this.restrictedUntil = until;
    }

    public void liftRestriction() {
        this.restrictedUntil = null;
    }

    public void noShow(int quantity, BigDecimal amount) {
        this.cancelReserve(quantity, amount);
        this.warn();
    }

    public void resetWarn() {
        monthlyWarnCount = 0;
    }

    public void warn() {
        totalWarnCount++;
        monthlyWarnCount++;
    }

    public void warn(int time) {
        totalWarnCount += time;
        monthlyWarnCount += time;
    }

    private void assertNicknameChanged() {
        if (!changeName) {
            throw new UserValidateException("닉네임 변경 후 주문이 가능합니다.");
        }
    }
}
