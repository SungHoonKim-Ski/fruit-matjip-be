package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.repository.entity.base.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Users extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String socialId;

    @Getter
    @Column(nullable = false, length = 50)
    private String internalUid;

    @Getter
    @Column(nullable = false, length = 50)
    private String name;

    private LocalDate lastOrderDate;

    @Column(nullable = false)
    private Long totalOrders = 0L;

    private LocalDateTime deletedAt;

    @Builder
    public Users(String socialId, String name, UUID uuid) {
        this.socialId = socialId;
        this.internalUid = uuid.toString();
        this.name = name;
    }

    public void modifyName(String name) {
        this.name = name;
    }
}
