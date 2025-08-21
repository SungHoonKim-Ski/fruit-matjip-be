package store.onuljang.controller.request;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

public record AdminUpdateProductDetailsRequest(
    @Size(max = 100, message = "상품명은 100자 이하여야 합니다")
    String name,

    @Min(value = 100, message = "가격은 100원 이상이어야 합니다")
    @Max(value = 1_000_000, message = "가격은 1,000,000원 이하여야 합니다")
    Integer price,

    @Min(value = 0, message = "재고 변경값은 0 이상이어야 합니다")
    Integer stock,

    @Size(max = 300, message = "이미지 경로는 300자 이하여야 합니다")
    @Pattern(regexp = "^[A-Za-z0-9._/\\-:]*$", message = "이미지 경로에는 영문/숫자/._-/:/만 허용됩니다")
    String productUrl,

    @Pattern(regexp = "^(|\\d{4}-\\d{2}-\\d{2})$", message = "판매일은 빈 문자열 또는 YYYY-MM-DD 형식이어야 합니다")
    String sellDate,

    @Size(max = 2000, message = "설명은 300자 이하여야 합니다")
    String description,

    @Size(max = 5, message = "상세 이미지는 최대 5개까지 가능합니다")
    @UniqueElements(message = "상세 이미지에는 중복이 있을 수 없습니다")
    List<
    @NotBlank(message = "상세 이미지 경로는 비어 있을 수 없습니다")
    @Size(max = 300, message = "상세 이미지 경로는 300자 이하여야 합니다")
    @Pattern(regexp = "^[A-Za-z0-9._/\\-:]+$", message = "상세 이미지 경로에는 영문/숫자/._-/:/만 허용됩니다")
        String
    > detailUrls
) {}