package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.ProductOrderRepository;
import store.onuljang.repository.entity.ProductOrder;

import java.time.LocalDate;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class ProductOrderService {
    ProductOrderRepository productOrderRepository;

    @Transactional
    public void deleteAllBySellDate(LocalDate date) {
        productOrderRepository.deleteAllBySellDate(date);
    }

    @Transactional
    public int saveAll(List<ProductOrder> entities) {
        return productOrderRepository.saveAll(entities).size();
    }
}
