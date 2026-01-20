package store.onuljang.controller.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminUpdateCategoryRequest(@Size(max = 5, message = "카테고리 명칭은 5자 이하여야 합니다") String name,
        String imageUrl) {
}
