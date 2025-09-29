package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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
        BigDecimal price,
        LocalDate sellDate,
        LocalTime sellTime,
        boolean visible,
        boolean selfPick,
        int orderIndex,
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
                .sellTime(product.getSellTime())
                .visible(product.getVisible())
                .orderIndex(product.getOrderIndex())
                .selfPick(product.getSelfPick())
                .totalSold(product.getTotalSold())
                .build();
        }
    }

    public static ProductListResponse from(List<Product> productList) {
        return ProductListResponse.builder()
            .response(productList.stream().map(ProductResponse::from).toList())
            .build();
    }
}