package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import store.onuljang.repository.entity.base.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Users extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String socialId;

    @Column(nullable = false)
    private UUID internalId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private LocalDate joinedDate;

    private LocalDate lastOrderDate;

    @Column(nullable = false)
    private Long totalOrders = 0L;

    private LocalDateTime deletedAt;
}
