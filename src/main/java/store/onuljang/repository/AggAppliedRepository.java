package store.onuljang.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.repository.entity.AggApplied;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AggAppliedRepository extends JpaRepository<AggApplied, Long> {

    // 배치 예정 컬럼 INSERT - 주로 노쇼 주문에 사용
    @Modifying(flushAutomatically = true)
    @Query(value = """
        insert IGNORE into agg_applied (reservation_id, phase)
        values (:rid, :phase)
    """, nativeQuery = true)
    int markSingle(@Param("rid") long reservationId, @Param("phase") String phase);

    // 1. 배치 데이터 INSERT
    @Modifying
    @Query(value = """
        insert IGNORE into agg_applied (reservation_id, phase)
            select r.id,
            case r.status
                when 'PICKED'          then 'PICKED_PLUS'
                when 'SELF_PICK_READY' then 'SELF_PICKUP_READY_PLUS'
            end as phase
        from reservations r
        where r.pickup_date = :date
        and r.status in ('PICKED', 'SELF_PICK_READY')
    """, nativeQuery = true)
    int markBatchForDay(@Param("date") LocalDate date);

    // 2. 배치 마킹(매출 정산 + 노쇼분 차감)
    @Modifying(flushAutomatically = true)
    @Query(value = """
        update agg_applied
            set batch_uid = :batchUid
        where processed = 0
            and batch_uid is null
            and phase in ('PICKED_PLUS','SELF_PICKUP_READY_PLUS','NO_SHOW_MINUS')
    """, nativeQuery = true)
    int claimUnprocessed(@Param("batchUid") String batchUid);

    // 3. 배치 종료
    @Modifying
    @Query(value = """
        update agg_applied
        set processed = 1,
            processed_at = :now,
            batch_uid = NULL
        WHERE batch_uid = :batchUid
        AND processed = 0
    """, nativeQuery = true)
    int finishBatch(@Param("batchUid") String batchUid, @Param("now") LocalDateTime now);
}
