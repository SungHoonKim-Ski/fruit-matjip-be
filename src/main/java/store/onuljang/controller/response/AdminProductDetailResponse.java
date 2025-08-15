package store.onuljang.controller.response;


import lombok.Builder;
import store.onuljang.repository.entity.Product;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AdminProductDetailResponse (
    long id,
    String name,
    long price,
    int stock,
    long totalSold,
    String productUrl,
    List<String> detailUrls,
    LocalDate sellDate,
    String description
){
    public static AdminProductDetailResponse from(Product product) {
        return AdminProductDetailResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .stock(product.getStock())
            .description(product.getDescription())
            .productUrl(product.getProductUrl())
            .detailUrls(product.getDetailImages())
            .sellDate(product.getSellDate())
            .description(product.getDescription())
            .build();
    }
}