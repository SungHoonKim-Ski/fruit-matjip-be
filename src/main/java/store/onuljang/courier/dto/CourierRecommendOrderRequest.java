package store.onuljang.courier.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CourierRecommendOrderRequest(
    @NotNull List<Long> productIds
) {}
