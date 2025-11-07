package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.ProductKeywordRepository;
import store.onuljang.repository.entity.ProductKeyword;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class ProductKeywordService {
    ProductKeywordRepository productKeywordRepository;

    @Transactional
    public void save(ProductKeyword productKeyword) {
        productKeywordRepository.save(productKeyword);
    }

    @Transactional
    public void saveAll(List<ProductKeyword> productKeywords) {
        productKeywordRepository.saveAll(productKeywords);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(String keyword) {
        productKeywordRepository.deleteByName(keyword);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllWithNewTransaction() {
        productKeywordRepository.deleteAll();
    }

    @Transactional
    public boolean existKeyword(String keyword) {
        return !productKeywordRepository.findAllByName(keyword).isEmpty();
    }

    @Transactional(readOnly = true)
    public List<ProductKeyword> findAll() {
        return productKeywordRepository.findAll();
    }
}
