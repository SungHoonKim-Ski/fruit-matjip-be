package store.onuljang.shop.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import store.onuljang.shop.admin.entity.Admin;

public record AdminSignupRequest(
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 1, max = 5, message = "이름은 1자 이상 5자 이하여야 합니다")
    String name,

    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 5, max = 15, message = "아이디는 5자 이상 15자 이하여야 합니다")
    @Pattern(
        regexp = "^[a-zA-Z0-9]+$",
        message = "아이디는 영문자와 숫자만 사용 가능합니다"
    )
    String email,

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 20, message = "비밀번호는 최소 8자 이상이어야 합니다")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$",
        message = "비밀번호는 영문자와 숫자를 포함해야 합니다"
    )
    String password
) {
    public static Admin toEntity(AdminSignupRequest request, String encodedPassword) {
        return Admin.builder()
            .name(request.name)
            .email(request.email)
            .password(encodedPassword)
            .build();
    }
}
