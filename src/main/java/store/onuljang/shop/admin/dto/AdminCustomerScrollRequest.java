package store.onuljang.shop.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AdminCustomerScrollRequest(
    String name,
    AdminCustomerSortKey sortKey,
    SortOrder sortOrder,
    String cursor,
    @Min(1) @Max(100)
    int limit
) { }
