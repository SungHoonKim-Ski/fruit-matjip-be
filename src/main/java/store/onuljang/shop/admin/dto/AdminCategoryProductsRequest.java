package store.onuljang.shop.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record AdminCategoryProductsRequest(
        @NotNull
        @Valid
        List<@PositiveOrZero Long> productIds) {
}
