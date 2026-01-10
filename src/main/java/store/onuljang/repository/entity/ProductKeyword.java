package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.repository.entity.base.BaseLogEntity;

import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_keyword")
public class ProductKeyword extends BaseLogEntity {
    @Getter
    @Column(name = "name", nullable = false)
    private String name;
}
