package store.onuljang.courier.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.courier.dto.CourierCategoryResponse;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.entity.CourierProductCategory;
import store.onuljang.courier.service.CourierProductCategoryService;

@RestController
@RequestMapping("/api/admin/courier/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AdminCourierCategoryController {

    CourierProductCategoryService courierProductCategoryService;

    @GetMapping
    public ResponseEntity<CourierCategoryResponse> getCategories() {
        return ResponseEntity.ok(
                CourierCategoryResponse.of(
                        courierProductCategoryService.findAllOrderBySortOrder()));
    }

    @PostMapping
    public ResponseEntity<Void> createCategory(
            @Valid @RequestBody CreateCourierCategoryRequest request) {
        if (courierProductCategoryService.existsByName(request.name())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리입니다: " + request.name());
        }
        CourierProductCategory category =
                CourierProductCategory.builder()
                        .name(request.name())
                        .imageUrl(request.imageUrl())
                        .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                        .build();
        courierProductCategoryService.save(category);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourierCategoryRequest request) {
        CourierProductCategory category =
                courierProductCategoryService
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "존재하지 않는 카테고리: " + id));
        if (request.name() != null) category.setName(request.name());
        if (request.imageUrl() != null) category.setImageUrl(request.imageUrl());
        if (request.sortOrder() != null) category.setSortOrder(request.sortOrder());
        courierProductCategoryService.save(category);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        CourierProductCategory category =
                courierProductCategoryService
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "존재하지 않는 카테고리: " + id));
        courierProductCategoryService.delete(category.getName());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/order")
    public ResponseEntity<Void> updateOrder(@RequestBody List<Long> categoryIds) {
        List<CourierProductCategory> categories =
                courierProductCategoryService.findAllOrderBySortOrder();
        Map<Long, CourierProductCategory> map =
                categories.stream()
                        .collect(
                                Collectors.toMap(
                                        CourierProductCategory::getId, c -> c));
        int idx = 0;
        for (Long catId : categoryIds) {
            CourierProductCategory cat = map.get(catId);
            if (cat != null) {
                cat.setSortOrder(idx++);
                courierProductCategoryService.save(cat);
            }
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/products")
    public ResponseEntity<Void> replaceCategoryProducts(
            @PathVariable Long id,
            @Valid @RequestBody CourierCategoryProductsRequest request) {
        courierProductCategoryService.replaceProducts(id, request.productIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<CourierCategoryProductsResponse> getCategoryProducts(
            @PathVariable Long id) {
        List<CourierProduct> products = courierProductCategoryService.getProductsByCategory(id);
        return ResponseEntity.ok(CourierCategoryProductsResponse.of(products));
    }

    public record CreateCourierCategoryRequest(
            @NotBlank String name, String imageUrl, Integer sortOrder) {}

    public record UpdateCourierCategoryRequest(
            String name, String imageUrl, Integer sortOrder) {}

    public record CourierCategoryProductsRequest(@NotNull List<Long> productIds) {}

    public record CourierCategoryProductsResponse(List<CategoryProductItem> response) {
        public record CategoryProductItem(
                Long id, String name, BigDecimal price, String productUrl, Integer stock) {}

        public static CourierCategoryProductsResponse of(List<CourierProduct> products) {
            return new CourierCategoryProductsResponse(
                    products.stream()
                            .map(
                                    p ->
                                            new CategoryProductItem(
                                                    p.getId(),
                                                    p.getName(),
                                                    p.getPrice(),
                                                    p.getProductUrl(),
                                                    p.getStock()))
                            .toList());
        }
    }
}
