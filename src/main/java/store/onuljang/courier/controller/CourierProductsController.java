package store.onuljang.courier.controller;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import store.onuljang.courier.appservice.CourierProductsAppService;
import store.onuljang.courier.dto.CourierProductListResponse;
import store.onuljang.courier.dto.CourierProductResponse;
import store.onuljang.courier.dto.CourierProductsByCategoryResponse;
import store.onuljang.courier.dto.CourierCategoryResponse;

@RestController
@RequestMapping("/api/auth/courier/products")
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
    public ResponseEntity<CourierCategoryResponse> categories() {
        return ResponseEntity.ok(courierProductsAppService.getProductCategories());
    }

    @GetMapping("/recommended")
    public ResponseEntity<CourierProductListResponse> getRecommendedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(courierProductsAppService.getRecommendedProducts(limit));
    }

    @GetMapping("/search")
    public ResponseEntity<CourierProductListResponse> searchProducts(
            @RequestParam String q) {
        return ResponseEntity.ok(courierProductsAppService.searchProducts(q));
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<CourierProductsByCategoryResponse>> getProductsByCategory(
            @RequestParam(defaultValue = "4") int limit) {
        return ResponseEntity.ok(courierProductsAppService.getProductsByCategory(limit));
    }
}
