package store.onuljang.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AdminUpdateCategoryListRequest(@NotNull @Valid List<CategoryItemRequest> categories) {

    public record CategoryItemRequest(Long id, String name, String imageUrl) {
    }
}
