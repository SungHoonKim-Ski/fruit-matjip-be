package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.response.AdminReservationDetailsResponse;
import store.onuljang.controller.response.AdminReservationSummaryResponse;
import store.onuljang.repository.entity.ProductDailyAgg;
import store.onuljang.repository.entity.ProductDailyAggRow;
import store.onuljang.service.*;
import store.onuljang.util.TimeUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class AdminAggregationAppService {
    AggAppliedService aggAppliedService;
    ProductDailyAggService productDailyAggService;

    @Transactional
    public void aggregateReservation() {
        String batchUid = UUID.randomUUID().toString();

        int newlyMarked = aggAppliedService.markForDay(TimeUtil.yesterdayDate());
        int claimed = aggAppliedService.claimUnprocessed(batchUid);

        productDailyAggService.upsertForBatch(batchUid);

        int finished = aggAppliedService.finishBatch(batchUid, TimeUtil.nowDateTime());

        if (finished != claimed) {
            throw new IllegalStateException("finished != claimed: " + finished + " vs " + claimed);
        }

        log.info("[Aggregation] newlyMarked={}, claimed={}, finished={}", newlyMarked, claimed, finished);
    }

    @Transactional(readOnly = true)
    public AdminReservationSummaryResponse getAggregationSummary(LocalDate from, LocalDate to) {
        List<ProductDailyAggRow> rows = productDailyAggService.findAggBetween(from, to);

        return AdminReservationSummaryResponse.from(rows);
    }

    @Transactional(readOnly = true)
    public AdminReservationDetailsResponse getDetail(LocalDate date) {
        List<ProductDailyAgg> details = productDailyAggService.findDetailBySellDateWithProduct(date);

        return AdminReservationDetailsResponse.from(details);
    }
}
