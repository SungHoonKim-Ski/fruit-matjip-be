package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.exception.NotFoundException;
import store.onuljang.repository.ProductsRepository;
import store.onuljang.repository.entity.Product;

import java.time.LocalDate;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class ProductsService {
    ProductsRepository productsRepository;

    public Product findByIdWithLock(long id) {
        return productsRepository.findByIdWithLock(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 제품"));
    }

    public List<Product> findAllBetween(LocalDate from, LocalDate to) {
        return productsRepository.findAllBySellDateBetween(from, to);
    }

    public Product findByIdWithDetailImages(long id) {
        return productsRepository.findAllById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 제품"));
    }

    @Transactional
    public long save(Product product) {
        return productsRepository.save(product).getId();
    }
}
