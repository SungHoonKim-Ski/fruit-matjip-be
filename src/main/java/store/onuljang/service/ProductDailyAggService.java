package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.ProductDailyAggRepository;
import store.onuljang.repository.entity.ProductDailyAgg;
import store.onuljang.repository.entity.ProductDailyAggRow;

import java.time.LocalDate;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class ProductDailyAggService {
    ProductDailyAggRepository productDailyAggRepository;

    @Transactional
    public void upsertForBatch(String batchUid) {
        productDailyAggRepository.upsertForBatch(batchUid);
    }

    @Transactional(readOnly = true)
    public List<ProductDailyAggRow> findAggBetween(LocalDate from, LocalDate to) {
        return productDailyAggRepository.findAggBetween(from, to);
    }

    @Transactional(readOnly = true)
    public List<ProductDailyAgg> findDetailBySellDateWithProduct(LocalDate date) {
        return productDailyAggRepository.findAllBySellDate(date);
    }
}
