package store.onuljang.repository.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import store.onuljang.repository.entity.base.BaseEntity;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestEntity extends BaseEntity {
    public String name;
}
