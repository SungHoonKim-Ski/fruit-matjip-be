package store.onuljang.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

public record AdminProductUpdateOrder(
    @Size(min = 1, message = "순서 변경 제품은 1개 이상이어야 합니다.")
    @UniqueElements(message = "순서 변경 제품에는 중복이 있을 수 없습니다")
    List<
        @NotNull(message = "순서 변경 제품 아이디는 비어 있을 수 없습니다")
        @Positive(message = "순서 변경 제품 아이디는 양수여야 합니다.")
        Long
    > productIds
) {

}