package store.onuljang.controller;

import jakarta.validation.Valid;
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
import store.onuljang.appservice.ProductsAppService;
import store.onuljang.controller.response.ProductDetailResponse;
import store.onuljang.controller.response.ProductListResponse;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth/products")
@RequiredArgsConstructor
@Validated
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductsController {
    ProductsAppService productsAppService;

    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(
       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @FutureOrPresent @NotNull LocalDate from,
       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @FutureOrPresent @NotNull LocalDate to
    ) {
        return ResponseEntity.ok(productsAppService.getProducts(from, to));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> detail(@PathVariable @PositiveOrZero Long id) {
        return ResponseEntity.ok(productsAppService.getDetail(id));
    }
}


