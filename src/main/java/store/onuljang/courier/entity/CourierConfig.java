package store.onuljang.courier.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Table(name = "courier_config")
public class CourierConfig extends BaseEntity {

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = false;

    @Column(name = "island_surcharge", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal islandSurcharge = new BigDecimal("3000");

    @Column(name = "base_shipping_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal baseShippingFee = new BigDecimal("3000");

    @Column(name = "combined_shipping_enabled", nullable = false)
    @Builder.Default
    private boolean combinedShippingEnabled = false;

    @Column(name = "combined_shipping_max_quantity", nullable = false)
    @Builder.Default
    private int combinedShippingMaxQuantity = 1;

    @Lob
    @Column(name = "notice_text", columnDefinition = "TEXT")
    private String noticeText;

    @Column(name = "sender_name", length = 50)
    private String senderName;

    @Column(name = "sender_phone", length = 30)
    private String senderPhone;

    @Column(name = "sender_phone2", length = 30)
    private String senderPhone2;

    @Column(name = "sender_address", length = 200)
    private String senderAddress;

    @Column(name = "sender_detail_address", length = 200)
    private String senderDetailAddress;

    public void update(
            boolean enabled,
            BigDecimal islandSurcharge,
            BigDecimal baseShippingFee,
            boolean combinedShippingEnabled,
            int combinedShippingMaxQuantity,
            String noticeText,
            String senderName,
            String senderPhone,
            String senderPhone2,
            String senderAddress,
            String senderDetailAddress) {
        this.enabled = enabled;
        this.islandSurcharge = islandSurcharge;
        this.baseShippingFee = baseShippingFee;
        this.combinedShippingEnabled = combinedShippingEnabled;
        this.combinedShippingMaxQuantity = combinedShippingMaxQuantity;
        this.noticeText = noticeText;
        this.senderName = senderName;
        this.senderPhone = senderPhone;
        this.senderPhone2 = senderPhone2;
        this.senderAddress = senderAddress;
        this.senderDetailAddress = senderDetailAddress;
    }
}
