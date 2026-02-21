package store.onuljang.courier.controller;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.courier.appservice.CourierProductsAppService;
import store.onuljang.courier.dto.CourierProductListResponse;
import store.onuljang.courier.dto.CourierProductResponse;
import store.onuljang.shop.product.dto.ProductCategoryResponse;

@RestController
@RequestMapping("/api/store/auth/courier/products")
@RequiredArgsConstructor
@Validated
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CourierProductsController {

    CourierProductsAppService courierProductsAppService;

    @GetMapping
    public ResponseEntity<CourierProductListResponse> getProducts(
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(courierProductsAppService.getProducts(categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourierProductResponse> getDetail(
            @PathVariable @Positive Long id) {
        return ResponseEntity.ok(courierProductsAppService.getDetail(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<ProductCategoryResponse> categories() {
        return ResponseEntity.ok(courierProductsAppService.getProductCategories());
    }
}
