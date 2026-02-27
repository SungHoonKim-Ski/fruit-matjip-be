package store.onuljang.shop.delivery.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shop.delivery.entity.DeliveryDailyAggRow;
import store.onuljang.shop.delivery.repository.DeliveryDailyAggRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class DeliveryDailyAggService {
    DeliveryDailyAggRepository deliveryDailyAggRepository;

    @Transactional
    public void upsertForDate(LocalDate sellDate) {
        deliveryDailyAggRepository.upsertForDate(sellDate);
    }

    @Transactional(readOnly = true)
    public List<DeliveryDailyAggRow> findAggBetween(LocalDate from, LocalDate to) {
        return deliveryDailyAggRepository.findAggBetween(from, to);
    }
}
