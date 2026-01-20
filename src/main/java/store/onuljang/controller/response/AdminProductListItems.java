package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
        LocalTime sellTime,
        int orderIndex,
        boolean visible,
        boolean selfPick
    ) {
        public static AdminProductListItem from(Product product) {
            return AdminProductListItem.builder()
                .id(product.getId())
                .name(product.getName())
                .productUrl(product.getProductUrl())
                .price(product.getPrice())
                .visible(product.getVisible())
                .selfPick(product.getSelfPick())
                .sellDate(product.getSellDate())
                .sellTime(product.getSellTime())
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
