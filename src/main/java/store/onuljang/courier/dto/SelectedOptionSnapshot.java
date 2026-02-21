package store.onuljang.courier.dto;

import java.math.BigDecimal;

public record SelectedOptionSnapshot(
    String groupName,
    String optionName,
    BigDecimal additionalPrice
) {}
