package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.exception.ProductExceedException;
import store.onuljang.exception.ProductUnavailableException;
import store.onuljang.repository.entity.base.BaseEntity;
import store.onuljang.repository.entity.base.BaseLogEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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