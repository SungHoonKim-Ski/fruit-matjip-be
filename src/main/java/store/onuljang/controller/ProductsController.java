package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.appservice.ProductsAppService;
import store.onuljang.controller.request.ProductListRequest;
import store.onuljang.controller.response.ProductDetailResponse;
import store.onuljang.controller.response.ProductListResponse;

@RestController
@RequestMapping("/api/auth/products")
@RequiredArgsConstructor
@Validated
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductsController {
    ProductsAppService productsAppService;

    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(@Valid @ModelAttribute ProductListRequest request) {
        return ResponseEntity.ok(productsAppService.getProducts(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> detail(@PathVariable @Valid @PositiveOrZero Long id) {
        return ResponseEntity.ok(productsAppService.getDetail(id));
    }
}


