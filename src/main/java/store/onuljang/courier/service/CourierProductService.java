package store.onuljang.courier.service;

import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
                .findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 택배 상품"));
    }

    @Transactional
    public CourierProduct findByIdWithLock(long id) {
        return courierProductRepository
                .findByIdWithLock(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 택배 상품"));
    }

    public List<CourierProduct> findAllVisible() {
        return courierProductRepository.findByVisibleTrueOrderBySortOrderAsc();
    }

    public List<CourierProduct> findAllVisibleByCategory(Long categoryId) {
        return courierProductRepository.findAllVisibleByCategory(categoryId);
    }

    public List<CourierProduct> findAll() {
        return courierProductRepository.findAllByOrderBySortOrderAsc();
    }

    public List<CourierProduct> findAllByIdIn(List<Long> ids) {
        return courierProductRepository.findAllById(ids);
    }

    public CourierProduct findByIdWithOptionGroups(Long id) {
        return courierProductRepository
                .findByIdWithOptionGroups(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 택배 상품"));
    }

    public List<CourierProduct> findRecommendedProducts() {
        return courierProductRepository.findRecommendedProducts();
    }

    public List<CourierProduct> searchByName(String keyword) {
        return courierProductRepository.findByNameContaining(keyword);
    }

    public List<CourierProduct> findVisibleByCategoryLimited(Long categoryId, int limit) {
        return courierProductRepository.findVisibleByCategoryOrdered(
                categoryId, PageRequest.of(0, limit));
    }

    @Transactional
    public long save(CourierProduct product) {
        return courierProductRepository.save(product).getId();
    }
}
