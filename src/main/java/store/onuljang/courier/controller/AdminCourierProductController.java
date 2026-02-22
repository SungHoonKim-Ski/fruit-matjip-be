package store.onuljang.courier.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.courier.appservice.CourierAdminProductAppService;
import store.onuljang.courier.dto.CourierCategoryOrderRequest;
import store.onuljang.courier.dto.CourierProductCreateRequest;
import store.onuljang.courier.dto.CourierProductListResponse;
import store.onuljang.courier.dto.CourierProductResponse;
import store.onuljang.courier.dto.CourierProductUpdateRequest;
import store.onuljang.courier.dto.CourierRecommendOrderRequest;
import store.onuljang.shop.admin.dto.PresignedUrlRequest;
import store.onuljang.courier.dto.CourierCategoryResponse;
import store.onuljang.shop.admin.dto.PresignedUrlResponse;

@RestController
@RequestMapping("/api/admin/courier/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AdminCourierProductController {

    CourierAdminProductAppService courierAdminProductAppService;

    @GetMapping
    public ResponseEntity<CourierProductListResponse> getProducts() {
        return ResponseEntity.ok(courierAdminProductAppService.getProducts());
    }

    @PostMapping
    public ResponseEntity<CourierProductResponse> createProduct(
            @Valid @RequestBody CourierProductCreateRequest request) {
        return ResponseEntity.ok(courierAdminProductAppService.createProduct(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourierProductResponse> getProduct(
            @Valid @NotNull @Positive @PathVariable("id") Long id) {
        return ResponseEntity.ok(courierAdminProductAppService.getProduct(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CourierProductResponse> updateProduct(
            @Valid @NotNull @Positive @PathVariable("id") Long id,
            @Valid @RequestBody CourierProductUpdateRequest request) {
        return ResponseEntity.ok(courierAdminProductAppService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Valid @NotNull @Positive @PathVariable("id") Long id) {
        courierAdminProductAppService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/order")
    public ResponseEntity<Integer> updateOrder(@Valid @RequestBody List<Long> productIds) {
        return ResponseEntity.ok(courierAdminProductAppService.updateOrder(productIds));
    }

    @PatchMapping("/category-order")
    public ResponseEntity<Integer> updateCategoryOrder(
            @Valid @RequestBody CourierCategoryOrderRequest request) {
        return ResponseEntity.ok(
                courierAdminProductAppService.updateCategoryOrder(
                        request.categoryId(), request.productIds()));
    }

    @PatchMapping("/visible/{id}")
    public ResponseEntity<Void> toggleVisible(
            @Valid @NotNull @Positive @PathVariable("id") Long id) {
        courierAdminProductAppService.toggleVisible(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/sold-out/{id}")
    public ResponseEntity<Void> toggleSoldOut(
            @Valid @NotNull @Positive @PathVariable("id") Long id) {
        courierAdminProductAppService.toggleSoldOut(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<CourierCategoryResponse> getCategories() {
        return ResponseEntity.ok(courierAdminProductAppService.getCategories());
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @Valid @RequestBody PresignedUrlRequest request) {
        return ResponseEntity.ok(courierAdminProductAppService.getPresignedUrl(request));
    }

    @PatchMapping("/{id}/recommend")
    public ResponseEntity<Void> toggleRecommend(
            @PathVariable("id") Long id) {
        courierAdminProductAppService.toggleRecommend(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/recommend-order")
    public ResponseEntity<Integer> updateRecommendOrder(
            @Valid @RequestBody CourierRecommendOrderRequest request) {
        return ResponseEntity.ok(
            courierAdminProductAppService.updateRecommendOrder(request.productIds()));
    }
}
