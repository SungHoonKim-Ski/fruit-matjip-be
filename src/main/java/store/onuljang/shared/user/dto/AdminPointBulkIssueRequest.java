package store.onuljang.shared.user.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record AdminPointBulkIssueRequest(
        List<String> uids,
        boolean allUsers,
        @NotNull @DecimalMin(value = "1", message = "1원 이상이어야 합니다.") BigDecimal amount,
        @NotBlank String description) {}
