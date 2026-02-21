package store.onuljang.shared.user.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_delivery_info")
public class UserDeliveryInfo extends BaseEntity {

    @Getter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_uid",
        referencedColumnName = "uid",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_user_delivery_info_user")
    )
    private Users user;

    @Getter
    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Getter
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Getter
    @Column(name = "address1", nullable = false, length = 200)
    private String address1;

    @Getter
    @Column(name = "address2", length = 200)
    private String address2;

    @Getter
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Getter
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    public void update(String phone, String postalCode, String address1, String address2, Double latitude, Double longitude) {
        this.phone = phone;
        this.postalCode = postalCode;
        this.address1 = address1;
        this.address2 = address2;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
