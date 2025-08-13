package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.Product;

import java.time.LocalDate;
import java.util.List;

@Builder
public record ProductDetailResponse(
    Long id,
    String name,
    int price,
    String imageUrl,
    String description,
    List<String> detailImages
) {
    public static ProductDetailResponse from(Product entity) {
        return ProductDetailResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .price(entity.getPrice())
            .imageUrl(entity.getProductUrl())
            .description(entity.getDescription())
            .detailImages(entity.getDetailImages())
            .build();
    }
}