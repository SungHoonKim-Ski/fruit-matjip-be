package store.onuljang.shared.user.dto;

import java.math.BigDecimal;

public record AdminPointBulkIssueResponse(int successCount, int failCount, BigDecimal totalAmount) {}
