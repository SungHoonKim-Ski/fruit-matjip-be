package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.response.ProductDetailResponse;
import store.onuljang.controller.response.ProductListResponse;
import store.onuljang.repository.entity.Product;
import store.onuljang.service.ProductsService;

import java.time.LocalDate;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductsAppService {
    ProductsService productsService;

    @Transactional(readOnly = true)
    public ProductListResponse getProducts(LocalDate from, LocalDate to) {
        List<Product> products = productsService.findAllBetween(from, to);

        return ProductListResponse.from(products);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(long id) {
        Product products = productsService.findByIdWithDetailImages(id);

        return ProductDetailResponse.from(products);
    }
}
