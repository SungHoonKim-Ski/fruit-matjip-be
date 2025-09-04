package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.AggAppliedRepository;
import store.onuljang.repository.entity.enums.AggPhase;

import java.time.LocalDate;

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
        return aggAppliedRepository.markSingle(reservationId, phase.name());
    }

    @Transactional
    public int claimUnprocessed(String batchUid) {
        return aggAppliedRepository.claimUnprocessed(batchUid);
    }

    @Transactional
    public int finishBatch(String batchUid) {
        return aggAppliedRepository.finishBatch(batchUid);
    }
}
