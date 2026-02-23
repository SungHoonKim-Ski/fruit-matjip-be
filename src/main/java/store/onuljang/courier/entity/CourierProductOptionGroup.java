package store.onuljang.courier.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import store.onuljang.shared.entity.base.BaseEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Table(name = "courier_product_option_groups")
public class CourierProductOptionGroup extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "courier_product_id", nullable = false)
    private CourierProduct courierProduct;

    @Setter
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Setter
    @Column(name = "required", nullable = false)
    @Builder.Default
    private Boolean required = true;

    @Setter
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    @BatchSize(size = 200)
    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<CourierProductOption> options = new ArrayList<>();

    public void addOption(CourierProductOption option) {
        this.options.add(option);
    }
}
