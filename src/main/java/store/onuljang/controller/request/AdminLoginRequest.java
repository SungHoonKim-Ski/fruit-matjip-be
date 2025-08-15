package store.onuljang.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminLoginRequest(
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
}
