package store.onuljang.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import store.onuljang.repository.ProductCategoryRepository;
import store.onuljang.repository.entity.ProductCategory;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductCategoryService {

    ProductCategoryRepository productCategoryRepository;

    public void save(ProductCategory productCategory) {
        productCategoryRepository.save(productCategory);
    }

    @Transactional
    public void saveAll(List<ProductCategory> productCategories) {
        productCategoryRepository.saveAll(productCategories);
    }

    @Transactional
    public void delete(String name) {
        productCategoryRepository.deleteByName(name);
    }

    @Transactional
    public void deleteAllWithFlush() {
        productCategoryRepository.deleteAllCategories();
    }

    public boolean existsByName(String name) {
        return !productCategoryRepository.findAllByName(name).isEmpty();
    }

    public List<ProductCategory> findAll() {
        return productCategoryRepository.findAllByOrderByCreatedAtAsc();
    }

    public List<ProductCategory> findAllOrderBySortOrder() {
        return productCategoryRepository.findAllByOrderBySortOrderAsc();
    }

    public Optional<ProductCategory> findById(Long id) {
        return productCategoryRepository.findById(id);
    }

    public Optional<ProductCategory> findByName(String name) {
        return productCategoryRepository.findByName(name);
    }
}
