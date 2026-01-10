package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import store.onuljang.repository.entity.base.BaseLogEntity;
import store.onuljang.repository.entity.enums.AggPhase;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "agg_applied")
public class AggApplied extends BaseLogEntity {
    @Column(name = "batch_uid", nullable = true, length = 36)
    private String batchUid;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "phase", nullable = false)
    @Enumerated(EnumType.STRING)
    private AggPhase phase;

    @Column(name = "processed", nullable = false)
    private Boolean processed;

    @Column(name = "processed_at", nullable = true)
    private LocalDateTime processedAt;
}
