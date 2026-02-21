package store.onuljang.courier.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.entity.CourierProductDetailImage;

@Builder
public record CourierProductResponse(
    Long id,
    String name,
    String productUrl,
    BigDecimal price,
    int stock,
    boolean visible,
    Integer weightGram,
    String description,
    int sortOrder,
    long totalSold,
    boolean recommended,
    int recommendOrder,
    List<String> detailImageUrls,
    List<CategoryItem> categories,
    LocalDateTime createdAt,
    Long shippingFeeTemplateId,
    String shippingFeeTemplateName,
    List<OptionGroupResponse> optionGroups
) {
    public record CategoryItem(Long id, String name) {}

    public static CourierProductResponse from(CourierProduct product) {
        List<String> imageUrls = product.getDetailImages().stream()
                .map(CourierProductDetailImage::getImageUrl)
                .toList();

        List<CategoryItem> categoryItems = product.getProductCategories().stream()
                .map(cat -> new CategoryItem(cat.getId(), cat.getName()))
                .toList();

        return CourierProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .productUrl(product.getProductUrl())
                .price(product.getPrice())
                .stock(product.getStock())
                .visible(product.getVisible())
                .weightGram(product.getWeightGram())
                .description(product.getDescription())
                .sortOrder(product.getSortOrder())
                .totalSold(product.getTotalSold())
                .recommended(product.getRecommended())
                .recommendOrder(product.getRecommendOrder())
                .detailImageUrls(imageUrls)
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
                .optionGroups(product.getOptionGroups().stream()
                        .map(OptionGroupResponse::from).toList())
                .build();
    }
}
