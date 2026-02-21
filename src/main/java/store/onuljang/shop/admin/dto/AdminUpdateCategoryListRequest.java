package store.onuljang.shop.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AdminUpdateCategoryListRequest(@NotNull @Valid List<CategoryItemRequest> categories) {

    public record CategoryItemRequest(
            Long id,
            @Size(max = 5, message = "카테고리 명칭은 5자 이하여야 합니다") String name,
            String imageUrl) {
    }
}
