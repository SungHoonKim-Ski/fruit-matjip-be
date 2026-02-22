package store.onuljang.courier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record OptionRequest(
    @NotBlank @Size(max = 50) String name,
    BigDecimal additionalPrice,
    Integer sortOrder,
    Integer stock
) {}
