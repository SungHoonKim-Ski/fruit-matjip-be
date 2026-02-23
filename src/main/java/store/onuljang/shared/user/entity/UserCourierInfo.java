package store.onuljang.shared.user.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_courier_info")
public class UserCourierInfo extends BaseEntity {

    @Getter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_uid",
        referencedColumnName = "uid",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_user_courier_info_user")
    )
    private Users user;

    @Getter
    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName;

    @Getter
    @Column(name = "receiver_phone", nullable = false, length = 30)
    private String receiverPhone;

    @Getter
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Getter
    @Column(name = "address1", nullable = false, length = 200)
    private String address1;

    @Getter
    @Column(name = "address2", length = 200)
    private String address2;

    public void update(String receiverName, String receiverPhone, String postalCode, String address1, String address2) {
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.postalCode = postalCode;
        this.address1 = address1;
        this.address2 = address2;
    }
}
