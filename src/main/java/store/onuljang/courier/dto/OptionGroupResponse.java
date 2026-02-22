package store.onuljang.courier.dto;

import java.math.BigDecimal;
import java.util.List;
import store.onuljang.courier.entity.CourierProductOption;
import store.onuljang.courier.entity.CourierProductOptionGroup;

public record OptionGroupResponse(
    Long id,
    String name,
    boolean required,
    int sortOrder,
    List<OptionItemResponse> options
) {
    public record OptionItemResponse(
        Long id,
        String name,
        BigDecimal additionalPrice,
        int sortOrder,
        Integer stock
    ) {
        public static OptionItemResponse from(CourierProductOption o) {
            return new OptionItemResponse(o.getId(), o.getName(), o.getAdditionalPrice(), o.getSortOrder(), o.getStock());
        }
    }

    public static OptionGroupResponse from(CourierProductOptionGroup g) {
        return new OptionGroupResponse(
                g.getId(),
                g.getName(),
                g.getRequired(),
                g.getSortOrder(),
                g.getOptions().stream().map(OptionItemResponse::from).toList());
    }
}
