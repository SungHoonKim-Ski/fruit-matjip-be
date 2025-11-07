package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.response.ProductDetailResponse;
import store.onuljang.controller.response.ProductKeywordResponse;
import store.onuljang.controller.response.ProductListResponse;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.ProductKeyword;
import store.onuljang.service.ProductKeywordService;
import store.onuljang.service.ProductsService;

import java.time.LocalDate;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductsAppService {
    ProductsService productsService;
    ProductKeywordService productKeywordService;

    @Transactional(readOnly = true)
    public ProductKeywordResponse getProductKeywords() {
        List<ProductKeyword> keywords = productKeywordService.findAll();

        return ProductKeywordResponse.of(keywords);
    }

    @Transactional(readOnly = true)
    public ProductListResponse getProducts(LocalDate from, LocalDate to) {
        List<Product> products = productsService.findAllVisibleBetween(from, to, true);

        return ProductListResponse.from(products);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(long id) {
        Product products = productsService.findByIdWithDetailImages(id);

        return ProductDetailResponse.from(products);
    }
}
