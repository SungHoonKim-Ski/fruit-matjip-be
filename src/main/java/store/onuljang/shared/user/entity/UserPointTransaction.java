package store.onuljang.shared.user.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import store.onuljang.shared.entity.base.BaseEntity;
import store.onuljang.shared.entity.enums.UserPointTransactionType;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_point_transactions")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPointTransaction extends BaseEntity {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    Users user;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    UserPointTransactionType type;

    @Getter
    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal amount;

    @Getter
    @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
    BigDecimal balanceAfter;

    @Getter
    @Column(nullable = false, length = 100)
    String description;

    @Getter
    @Column(name = "reference_type", length = 30)
    String referenceType;

    @Getter
    @Column(name = "reference_id")
    Long referenceId;

    @Getter
    @Column(name = "created_by", length = 50)
    String createdBy;
}
