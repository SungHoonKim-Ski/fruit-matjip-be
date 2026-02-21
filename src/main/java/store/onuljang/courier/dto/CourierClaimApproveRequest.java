package store.onuljang.courier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CourierClaimApproveRequest(
        @NotNull String action, @NotBlank String adminNote, BigDecimal refundAmount) {}
