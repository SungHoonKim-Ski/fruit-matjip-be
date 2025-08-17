package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.repository.entity.base.BaseEntity;

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
    @Column(nullable = false, length = 50)
    private String uid;

    @Getter
    @Column(nullable = false, length = 50)
    private String name;

    private LocalDate lastOrderDate;

    @Column(nullable = false)
    private Long totalOrders = 0L;

    private LocalDateTime deletedAt;

    @Builder
    public Users(String socialId, String name, UUID uid) {
        this.socialId = socialId;
        this.uid = uid.toString();
        this.name = name;
    }

    public void modifyName(String name) {
        this.name = name;
    }

    public void addTotalOrders(long totalOrders) {
        this.totalOrders = Math.max(this.totalOrders, this.totalOrders + totalOrders);
    }

    public void removeTotalOrders(long totalOrders) {
        this.totalOrders = Math.max(this.totalOrders - totalOrders, 0);
    }

    public void reserve(int quantity) {
        lastOrderDate = LocalDate.now();
        addTotalOrders(quantity);
    }
}
