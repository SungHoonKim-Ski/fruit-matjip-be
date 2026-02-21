package store.onuljang.shop.product.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shop.product.repository.AggAppliedRepository;
import store.onuljang.shared.entity.enums.AggPhase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class AggAppliedService {
    AggAppliedRepository aggAppliedRepository;

    @Transactional
    public int markForDay(LocalDate date) {
        return aggAppliedRepository.markBatchForDay(date);
    }

    @Transactional
    public int markSingle(long reservationId, AggPhase phase) {
        return aggAppliedRepository.insertAppliedIgnoreDuplicate(reservationId, phase.name());
    }

    @Transactional
    public int markMany(Set<Long> reservationIds, AggPhase phase) {
        return aggAppliedRepository.bulkInsertAppliedIgnoreDuplicate(reservationIds, phase.name());
    }

    @Transactional
    public int claimUnprocessed(String batchUid) {
        return aggAppliedRepository.claimUnprocessed(batchUid);
    }

    @Transactional
    public int finishBatch(String batchUid, LocalDateTime now) {
        return aggAppliedRepository.finishBatch(batchUid, now);
    }
}
