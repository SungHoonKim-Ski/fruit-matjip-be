package store.onuljang.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.controller.request.*;
import store.onuljang.controller.response.AdminProductDetailResponse;
import store.onuljang.controller.response.AdminProductListItems;
import store.onuljang.appservice.AdminProductAppService;
import store.onuljang.controller.response.ProductCategoryResponse;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AdminProductController {
    AdminProductAppService adminProductAppService;

    @GetMapping
    public ResponseEntity<AdminProductListItems> getAll() {
        return ResponseEntity.ok(adminProductAppService.getAll());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<AdminProductDetailResponse> getDetail(
            @Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        return ResponseEntity.ok(adminProductAppService.getDetail(productId));
    }

    @PostMapping
    public ResponseEntity<Long> create(@Valid @RequestBody AdminCreateProductRequest request) {
        return ResponseEntity.ok(adminProductAppService.save(request));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Void> updateDetail(
        @Valid @NotNull @Positive @PathVariable("productId") Long productId,
        @Valid @RequestBody AdminUpdateProductDetailsRequest request
    ) {
        adminProductAppService.updateDetail(productId, request);

        return ResponseEntity.ok().build();
    }

    @Deprecated
    @PatchMapping("/self-pick/{productId}")
    public ResponseEntity<Void> toggleSelfPick(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.toggleSelfPick(productId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/delivery-available/{productId}")
    public ResponseEntity<Void> toggleDeliveryAvailable(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.toggleDeliveryAvailable(productId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/visible/{productId}")
    public ResponseEntity<Void> toggleVisible(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.toggleVisible(productId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/bulk-sell-date")
    public ResponseEntity<Integer> bulkUpdateSellDate(
            @Valid @RequestBody AdminProductBulkUpdateSellDateRequest request) {
        return ResponseEntity.ok(adminProductAppService.bulkUpdateSellDate(request));
    }

    @PatchMapping("/order")
    public ResponseEntity<Integer> updateOrder(@Valid @RequestBody AdminProductUpdateOrderRequest request) {
        return ResponseEntity.ok(adminProductAppService.updateOrder(request));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.delete(productId);

        return ResponseEntity.ok().build();
    }

    // ===== Category Management =====

    @GetMapping("/categories")
    public ResponseEntity<ProductCategoryResponse> getProductCategories() {
        return ResponseEntity.ok(adminProductAppService.getProductCategories());
    }

    @PostMapping("/category")
    public ResponseEntity<Void> saveProductCategory(@Valid @RequestBody AdminCreateCategoryRequest request) {
        adminProductAppService.saveCategory(request);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/category/{categoryId}")
    public ResponseEntity<Void> updateProductCategory(
            @Valid @NotNull @Positive @PathVariable("categoryId") Long categoryId,
            @Valid @RequestBody AdminUpdateCategoryRequest request) {
        adminProductAppService.updateCategory(categoryId, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/category")
    public ResponseEntity<Void> deleteProductCategory(@NotEmpty @RequestParam String keyword) {
        adminProductAppService.deleteCategory(keyword);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/categories/{categoryId}/products")
    public ResponseEntity<Void> updateCategoryProducts(
            @Valid @NotNull @Positive @PathVariable("categoryId") Long categoryId,
            @Valid @RequestBody AdminCategoryProductsRequest request) {
        adminProductAppService.updateCategoryProducts(categoryId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/categories/order")
    public ResponseEntity<Void> updateCategorySortOrders(@RequestBody @Valid AdminUpdateCategoryListRequest request) {
        adminProductAppService.updateCategorySortOrders(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{productId}/categories/{categoryId}")
    public ResponseEntity<Void> addCategoryToProduct(
            @Valid @NotNull @Positive @PathVariable("productId") Long productId,
            @Valid @NotNull @Positive @PathVariable("categoryId") Long categoryId) {
        adminProductAppService.addCategoryToProduct(productId, categoryId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}/categories/{categoryId}")
    public ResponseEntity<Void> removeCategoryFromProduct(
            @Valid @NotNull @Positive @PathVariable("productId") Long productId,
            @Valid @NotNull @Positive @PathVariable("categoryId") Long categoryId) {
        adminProductAppService.removeCategoryFromProduct(productId, categoryId);
        return ResponseEntity.ok().build();
    }
}
