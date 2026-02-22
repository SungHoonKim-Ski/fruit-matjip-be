package store.onuljang.courier.service;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.entity.CourierProductCategory;
import store.onuljang.courier.entity.CourierProductCategoryMapping;
import store.onuljang.courier.repository.CourierProductCategoryMappingRepository;
import store.onuljang.courier.repository.CourierProductCategoryRepository;
import store.onuljang.courier.repository.CourierProductRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class CourierProductCategoryService {
    CourierProductCategoryRepository courierProductCategoryRepository;
    CourierProductCategoryMappingRepository courierProductCategoryMappingRepository;
    CourierProductRepository courierProductRepository;

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

    @Transactional
    public void replaceProducts(Long categoryId, List<Long> productIds) {
        CourierProductCategory category =
                courierProductCategoryRepository
                        .findById(categoryId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "존재하지 않는 카테고리: " + categoryId));
        courierProductCategoryMappingRepository.deleteAllByCourierProductCategoryId(categoryId);
        courierProductCategoryMappingRepository.flush();
        for (int i = 0; i < productIds.size(); i++) {
            Long productId = productIds.get(i);
            CourierProduct product =
                    courierProductRepository
                            .findById(productId)
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "존재하지 않는 상품: " + productId));
            CourierProductCategoryMapping mapping =
                    CourierProductCategoryMapping.builder()
                            .courierProduct(product)
                            .courierProductCategory(category)
                            .sortOrder(i)
                            .build();
            courierProductCategoryMappingRepository.save(mapping);
        }
    }

    public List<CourierProduct> getProductsByCategory(Long categoryId) {
        return courierProductCategoryMappingRepository
                .findByCategoryIdOrderBySortOrder(categoryId)
                .stream()
                .map(CourierProductCategoryMapping::getCourierProduct)
                .toList();
    }
}
