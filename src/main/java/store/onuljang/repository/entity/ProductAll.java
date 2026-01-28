package store.onuljang.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import store.onuljang.repository.entity.base.BaseEntity;

@Getter
@Immutable
@Entity
@Table(name = "products")
public class ProductAll extends BaseEntity {

    @Column(name = "product_url", nullable = false)
    private String productUrl;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "self_pick", nullable = false)
    private Boolean selfPick;

    @Column(name = "delivery_available", nullable = false)
    private Boolean deliveryAvailable;
}
