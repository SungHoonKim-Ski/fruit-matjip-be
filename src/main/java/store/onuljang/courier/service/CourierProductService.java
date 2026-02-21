package store.onuljang.courier.service;

import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.repository.CourierProductRepository;
import store.onuljang.shared.exception.NotFoundException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class CourierProductService {

    CourierProductRepository courierProductRepository;

    public CourierProduct findById(long id) {
        return courierProductRepository
                .findByIdAndNotDeleted(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 택배 상품"));
    }

    @Transactional
    public CourierProduct findByIdWithLock(long id) {
        return courierProductRepository
                .findByIdWithLock(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 택배 상품"));
    }

    public List<CourierProduct> findAllVisible() {
        return courierProductRepository.findAllVisible();
    }

    public List<CourierProduct> findAllVisibleByCategory(Long categoryId) {
        return courierProductRepository.findAllVisibleByCategory(categoryId);
    }

    public List<CourierProduct> findAllNotDeleted() {
        return courierProductRepository.findAllNotDeleted();
    }

    public CourierProduct findByIdWithDetailImages(long id) {
        return courierProductRepository
                .findByIdWithDetailImages(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 택배 상품"));
    }

    @Transactional
    public long save(CourierProduct product) {
        return courierProductRepository.save(product).getId();
    }
}
