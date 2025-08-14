package store.onuljang.controller.request;

import jakarta.validation.constraints.*;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;

import java.time.LocalDate;

public record AdminCreateProductRequest (
    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 20, message = "상품명은 20자 이하로 입력해주세요")
    String name,

    @NotNull(message = "가격은 필수입니다")
    @Min(value = 1, message = "가격은 0 이상이어야 합니다")
    Integer price,

    @NotNull(message = "재고 수량은 필수입니다")
    @Min(value = 1, message = "재고 수량은 0 이상이어야 합니다")
    Integer stock,

    @NotBlank(message = "이미지 URL은 필수입니다")
    String imageUrl,

    @NotBlank(message = "판매일은 필수입니다")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "판매일 형식이 올바르지 않습니다 (YYYY-MM-DD)")
    String sellDate,

    @NotNull(message = "상품 상태는 필수입니다")
    Boolean visible
) {
    public static Product toEntity(AdminCreateProductRequest request, String imageUrl, Admin admin) {
        return Product.builder()
            .price(request.price)
            .name(request.name)
            .stock(request.stock)
            .productUrl(imageUrl)
            .sellDate(LocalDate.parse(request.sellDate))
            .isVisible(request.visible)
            .registeredAdmin(admin)
            .build();
    }
}
