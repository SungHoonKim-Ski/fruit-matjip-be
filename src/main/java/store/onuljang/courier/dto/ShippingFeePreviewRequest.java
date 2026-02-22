package store.onuljang.courier.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record ShippingFeePreviewRequest(
        @NotEmpty @Valid List<ItemInfo> items,
        String postalCode) {

    public record ItemInfo(
            @NotNull @Positive Long courierProductId,
            @NotNull @Min(1) Integer quantity) {}
}
