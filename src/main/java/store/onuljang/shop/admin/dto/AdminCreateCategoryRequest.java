package store.onuljang.shop.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateCategoryRequest(
        @NotBlank(message = "카테고리 명칭은 필수입니다")
        @Size(max = 5, message = "카테고리 명칭은 5자 이하여야 합니다")
        String name,

        String imageUrl) {
}
