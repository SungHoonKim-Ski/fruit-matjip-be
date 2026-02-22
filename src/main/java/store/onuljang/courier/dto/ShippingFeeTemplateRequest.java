package store.onuljang.courier.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ShippingFeeTemplateRequest(
        @NotBlank(message = "템플릿 이름은 필수입니다")
        @Size(max = 50)
        String name,

        @NotNull(message = "기본 배송비는 필수입니다")
        @DecimalMin(value = "0")
        BigDecimal baseFee,

        @DecimalMin(value = "0")
        BigDecimal perQuantityFee,

        @DecimalMin(value = "0")
        BigDecimal freeShippingMinAmount,

        Integer sortOrder) {}
