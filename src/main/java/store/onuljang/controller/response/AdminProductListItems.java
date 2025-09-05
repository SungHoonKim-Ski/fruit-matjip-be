package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record AdminProductListItems(
    List<AdminProductListItem> response
) {
    @Builder
    public record AdminProductListItem(
        long id,
        String name,
        BigDecimal price,
        int stock,
        long totalSold,
        String productUrl,
        LocalDate sellDate,
        int orderIndex,
        boolean isVisible
    ) {
        public static AdminProductListItem from(Product product) {
            return AdminProductListItem.builder()
                .id(product.getId())
                .name(product.getName())
                .productUrl(product.getProductUrl())
                .price(product.getPrice())
                .isVisible(product.getIsVisible())
                .sellDate(product.getSellDate())
                .stock(product.getStock())
                .orderIndex(product.getOrderIndex())
                .totalSold(product.getTotalSold())
                .build();
        }
    }

    public static AdminProductListItems from(List<Product> products) {
        return AdminProductListItems.builder()
                .response(products.stream().map(AdminProductListItem::from).toList())
                .build();
    }
}
