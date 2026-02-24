package store.onuljang.courier.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import store.onuljang.courier.entity.CourierProduct;

@Builder
public record CourierProductResponse(
    Long id,
    String name,
    String productUrl,
    BigDecimal price,
    boolean visible,
    boolean soldOut,
    Integer weightGram,
    String description,
    int sortOrder,
    long totalSold,
    boolean recommended,
    int recommendOrder,
    List<CategoryItem> categories,
    LocalDateTime createdAt,
    Long shippingFeeTemplateId,
    String shippingFeeTemplateName,
    BigDecimal combinedShippingFee,
    List<OptionGroupResponse> optionGroups
) {
    public record CategoryItem(Long id, String name) {}

    public static CourierProductResponse from(CourierProduct product) {
        List<CategoryItem> categoryItems = product.getProductCategories().stream()
                .map(cat -> new CategoryItem(cat.getId(), cat.getName()))
                .toList();

        return CourierProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .productUrl(product.getProductUrl())
                .price(product.getPrice())
                .visible(product.getVisible())
                .soldOut(Boolean.TRUE.equals(product.getSoldOut()))
                .weightGram(product.getWeightGram())
                .description(product.getDescription())
                .sortOrder(product.getSortOrder())
                .totalSold(product.getTotalSold())
                .recommended(product.getRecommended())
                .recommendOrder(product.getRecommendOrder())
                .categories(categoryItems)
                .createdAt(product.getCreatedAt())
                .shippingFeeTemplateId(
                        product.getShippingFeeTemplate() != null
                                ? product.getShippingFeeTemplate().getId()
                                : null)
                .shippingFeeTemplateName(
                        product.getShippingFeeTemplate() != null
                                ? product.getShippingFeeTemplate().getName()
                                : null)
                .combinedShippingFee(product.getCombinedShippingFee())
                .optionGroups(product.getOptionGroups().stream()
                        .map(OptionGroupResponse::from).toList())
                .build();
    }
}
