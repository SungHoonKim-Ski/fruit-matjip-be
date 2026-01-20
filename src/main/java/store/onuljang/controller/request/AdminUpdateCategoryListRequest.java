package store.onuljang.controller.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminUpdateCategoryListRequest(@NotNull @Valid List<CategoryItemRequest> categories) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CategoryItemRequest(Long id, String name, String imageUrl) {
    }
}
