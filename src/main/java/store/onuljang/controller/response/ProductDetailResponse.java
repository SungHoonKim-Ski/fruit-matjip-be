package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.Product;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ProductDetailResponse(
    Long id,
    String name,
    BigDecimal price,
    String imageUrl,
    String description,
    List<String> detailImages,
    boolean deliveryAvailable
) {
    public static ProductDetailResponse from(Product entity) {
        return ProductDetailResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .price(entity.getPrice())
            .imageUrl(entity.getProductUrl())
            .description(entity.getDescription())
            .detailImages(entity.getDetailImages())
            .deliveryAvailable(entity.getDeliveryAvailable())
            .build();
    }
}
