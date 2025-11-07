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
import store.onuljang.controller.response.ProductKeywordResponse;

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
    public ResponseEntity<AdminProductDetailResponse> getDetail(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
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

    @PatchMapping("/self-pick/{productId}")
    public ResponseEntity<Void> toggleSelfPick(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.toggleSelfPick(productId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/visible/{productId}")
    public ResponseEntity<Void> toggleVisible(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.toggleVisible(productId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/bulk-sell-date")
    public ResponseEntity<Integer> bulkUpdateSellDate(@Valid @RequestBody AdminProductBulkUpdateSellDateRequest request) {
        return ResponseEntity.ok(adminProductAppService.bulkUpdateSellDate(request));
    }

    @PatchMapping("/order")
    public ResponseEntity<Integer> updateOrder(@Valid @RequestBody AdminProductUpdateOrder request) {
        return ResponseEntity.ok(adminProductAppService.updateOrder(request));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@Valid @NotNull @Positive @PathVariable("productId") Long productId) {
        adminProductAppService.delete(productId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/keywords")
    public ResponseEntity<ProductKeywordResponse> getProductKeyWord() {
        return ResponseEntity.ok(adminProductAppService.getProductKeywords());
    }

    @PostMapping("/keyword")
    public ResponseEntity<Void> saveProductKeyword(@NotEmpty @RequestParam String keyword) {
        adminProductAppService.saveKeyword(keyword);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/keyword")
    public ResponseEntity<Void> deleteProductKeyword(@NotEmpty @RequestParam String keyword) {
        adminProductAppService.deleteKeyword(keyword);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/keywords")
    public ResponseEntity<Void> updateProductKeyWord(@RequestBody @Valid AdminProductKeywordsRequest request) {
        adminProductAppService.updateKeywords(request.keywords());

        return ResponseEntity.ok().build();
    }
}