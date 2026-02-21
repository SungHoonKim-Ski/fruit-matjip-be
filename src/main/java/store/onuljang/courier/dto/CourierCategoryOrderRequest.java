package store.onuljang.courier.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CourierCategoryOrderRequest(
        @NotNull @Positive Long categoryId,
        @NotNull List<Long> productIds) {}
