package store.onuljang.shop.admin.dto;

import lombok.Builder;
import store.onuljang.shop.product.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record AdminProductDetailResponse (
    long id,
    String name,
    BigDecimal price,
    int stock,
    long totalSold,
    String productUrl,
    List<String> detailUrls,
    LocalDate sellDate,
    LocalTime sellTime,
    String description,
    boolean deliveryAvailable
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
            .sellTime(product.getSellTime())
            .description(product.getDescription())
            .deliveryAvailable(product.getDeliveryAvailable())
            .build();
    }
}
