package store.onuljang.courier.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record CourierProductCreateRequest(
    @NotBlank(message = "상품명은 필수입니다")
    @Size(min = 1, max = 100, message = "상품명은 100자 이하로 입력해주세요")
    String name,

    @NotBlank(message = "상품 이미지 URL은 필수입니다")
    String productUrl,

    @NotNull(message = "가격은 필수입니다")
    @Min(value = 100, message = "가격은 100원 이상이어야 합니다")
    BigDecimal price,

    Integer weightGram,

    String description,

    Integer sortOrder,

    List<Long> categoryIds,

    Long shippingFeeTemplateId,

    BigDecimal combinedShippingFee,

    List<OptionGroupRequest> optionGroups
) {}
