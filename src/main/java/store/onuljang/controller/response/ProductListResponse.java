package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.Product;

import java.time.LocalDate;
import java.util.List;

@Builder
public record ProductListResponse(
    List<ProductResponse> response
) {
    @Builder
    public record ProductResponse(
        Long id,
        String name,
        String imageUrl,
        int stock,
        int price,
        LocalDate sellDate,
        boolean isVisible,
        long totalSold
    ) {
        public static ProductResponse from(Product product) {
            return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .imageUrl(product.getProductUrl())
                .stock(product.getStock())
                .price(product.getPrice())
                .sellDate(product.getSellDate())
                .isVisible(product.getIsVisible())
                .totalSold(product.getTotalSold())
                .build();
        }
    }

    public static ProductListResponse from(List<Product> productList) {
        return ProductListResponse.builder()
            .response(productList.stream()
            .map(ProductResponse::from).toList())
            .build();
    }
}