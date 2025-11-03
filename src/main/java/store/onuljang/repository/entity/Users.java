package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.exception.UserValidateException;
import store.onuljang.repository.entity.base.BaseEntity;

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
    @Column
    private Integer warnCount = 0;

    @Getter
    @Column
    private Integer totalWarnCount = 0;

    @Builder
    public Users(String socialId, String name, UUID uid) {
        this.socialId = socialId;
        this.uid = uid.toString();
        this.name = name;
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
        if (add == null) return;
        this.totalRevenue = this.totalRevenue.add(add).max(BigDecimal.ZERO);
    }

    public void removeTotalRevenue(BigDecimal remove) {
        if (remove == null) return;

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

    public boolean exceedMaxWarnCount() {
        return warnCount >= getMaxWarnCount();
    }

    public void assertCanSelfPick() {
        if (exceedMaxWarnCount()) {
            throw new UserValidateException("이번 달 셀프 수령 취소 가능 횟수를 초과했습니다.");
        }
    }

    public void noShow(int quantity, BigDecimal amount) {
        this.cancelReserve(quantity, amount);
        this.warn();
    }

    public void resetWarn() {
        warnCount = 0;
    }

    public void warn() {
        totalWarnCount++;
        warnCount++;
    }

    private void assertNicknameChanged() {
        if (!changeName) {
            throw new UserValidateException("닉네임 변경 후 주문이 가능합니다.");
        }
    }

    private int getMaxWarnCount() {
        return 2;
    }
}
