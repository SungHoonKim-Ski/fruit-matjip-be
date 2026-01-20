package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.ProductCategoryRepository;
import store.onuljang.repository.entity.ProductCategory;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class ProductCategoryService {
    ProductCategoryRepository productCategoryRepository;

    public void save(ProductCategory productCategory) {
        productCategoryRepository.save(productCategory);
    }

    @Transactional
    public void delete(String name) {
        productCategoryRepository.deleteByName(name);
    }

    public boolean existsByName(String name) {
        return !productCategoryRepository.findAllByName(name).isEmpty();
    }

    public List<ProductCategory> findAll() {
        return productCategoryRepository.findAllByOrderBySortOrderAsc();
    }

    public List<ProductCategory> findAllOrderBySortOrder() {
        return productCategoryRepository.findAllByOrderBySortOrderAsc();
    }

    @Transactional
    public Optional<ProductCategory> findById(Long id) {
        return productCategoryRepository.findById(id);
    }
}
