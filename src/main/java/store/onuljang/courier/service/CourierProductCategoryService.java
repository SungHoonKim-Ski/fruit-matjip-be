package store.onuljang.courier.service;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.entity.CourierProductCategory;
import store.onuljang.courier.repository.CourierProductCategoryRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class CourierProductCategoryService {
    CourierProductCategoryRepository courierProductCategoryRepository;

    @Transactional
    public void save(CourierProductCategory category) {
        courierProductCategoryRepository.save(category);
    }

    @Transactional
    public void delete(String name) {
        courierProductCategoryRepository.deleteByName(name);
    }

    public boolean existsByName(String name) {
        return !courierProductCategoryRepository.findAllByName(name).isEmpty();
    }

    public List<CourierProductCategory> findAll() {
        return courierProductCategoryRepository.findAllByOrderBySortOrderAsc();
    }

    public List<CourierProductCategory> findAllOrderBySortOrder() {
        return courierProductCategoryRepository.findAllByOrderBySortOrderAsc();
    }

    public Optional<CourierProductCategory> findById(Long id) {
        return courierProductCategoryRepository.findById(id);
    }
}
