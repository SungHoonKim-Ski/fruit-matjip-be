package store.onuljang.controller.request;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.format.annotation.DateTimeFormat;
import store.onuljang.repository.entity.Admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record AdminProductBulkUpdateSellDateRequest(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @FutureOrPresent(message = "판매일은 과거일 수 없습니다.")
    LocalDate sellDate,

    @Size(min = 1, message = "판매일 변경 제품은 1개 이상이어야 합니다.")
    @UniqueElements(message = "판매일 변경 제품에는 중복이 있을 수 없습니다")
    Set<
        @NotNull(message = "판매일 변경 제품 아이디는 비어 있을 수 없습니다")
        @Positive(message = "판매일 변경 제품 아이디는 양수여야 합니다.")
        Long
    > productIds
) {

}