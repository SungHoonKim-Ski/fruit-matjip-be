package store.onuljang.shop.product.controller;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.shop.product.appservice.ProductsAppService;
import store.onuljang.shop.product.dto.ProductCategoryResponse;
import store.onuljang.shop.product.dto.ProductDetailResponse;
import store.onuljang.shop.product.dto.ProductListResponse;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/store/auth/products")
@RequiredArgsConstructor
@Validated
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductsController {
    ProductsAppService productsAppService;

    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @FutureOrPresent @NotNull LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @FutureOrPresent @NotNull LocalDate to,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(productsAppService.getProducts(from, to, categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> detail(@PathVariable @PositiveOrZero Long id) {
        return ResponseEntity.ok(productsAppService.getDetail(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<ProductCategoryResponse> categories() {
        return ResponseEntity.ok(productsAppService.getProductCategories());
    }
}
