package store.onuljang.shop.admin.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shop.admin.dto.AdminReservationDetailsResponse;
import store.onuljang.shop.admin.dto.AdminReservationSummaryResponse;
import store.onuljang.shop.delivery.entity.DeliveryDailyAggRow;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shop.product.entity.ProductDailyAgg;
import store.onuljang.shop.product.entity.ProductDailyAggRow;
import store.onuljang.shared.user.service.*;
import store.onuljang.shop.product.service.*;
import store.onuljang.shop.reservation.service.*;
import store.onuljang.shop.delivery.service.*;
import store.onuljang.shop.admin.service.*;
import store.onuljang.shared.auth.service.*;
import store.onuljang.shared.service.*;
import store.onuljang.shared.util.TimeUtil;

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
    DeliveryDailyAggService deliveryDailyAggService;
    DeliveryOrderService deliveryOrderService;

    @Transactional
    public void aggregateReservation() {
        LocalDate yesterday = TimeUtil.yesterdayDate();

        // 1. Auto-complete pending delivery orders for yesterday
        List<DeliveryOrder> pendingDeliveries =
                deliveryOrderService.findActiveByDeliveryDate(yesterday);
        pendingDeliveries.forEach(DeliveryOrder::markDelivered);
        int deliveryAutoCompleted = pendingDeliveries.size();

        // 2. Existing reservation aggregation
        String batchUid = UUID.randomUUID().toString();
        int newlyMarked = aggAppliedService.markForDay(yesterday);
        int claimed = aggAppliedService.claimUnprocessed(batchUid);

        productDailyAggService.upsertForBatch(batchUid);

        int finished = aggAppliedService.finishBatch(batchUid, TimeUtil.nowDateTime());

        if (finished != claimed) {
            throw new IllegalStateException("finished != claimed: " + finished + " vs " + claimed);
        }

        // 3. Delivery aggregation
        deliveryDailyAggService.upsertForDate(yesterday);

        log.info(
                "[Aggregation] newlyMarked={}, claimed={}, finished={}, deliveryAutoCompleted={}",
                newlyMarked,
                claimed,
                finished,
                deliveryAutoCompleted);
    }

    @Transactional(readOnly = true)
    public AdminReservationSummaryResponse getAggregationSummary(LocalDate from, LocalDate to) {
        List<ProductDailyAggRow> productRows = productDailyAggService.findAggBetween(from, to);
        List<DeliveryDailyAggRow> deliveryRows =
                deliveryDailyAggService.findAggBetween(from, to);

        return AdminReservationSummaryResponse.from(productRows, deliveryRows);
    }

    @Transactional(readOnly = true)
    public AdminReservationDetailsResponse getDetail(LocalDate date) {
        List<ProductDailyAgg> details = productDailyAggService.findDetailBySellDateWithProduct(date);

        return AdminReservationDetailsResponse.from(details);
    }
}
