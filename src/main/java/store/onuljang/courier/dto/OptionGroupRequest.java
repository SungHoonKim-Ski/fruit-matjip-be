package store.onuljang.courier.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OptionGroupRequest(
    @NotBlank @Size(max = 50) String name,
    Boolean required,
    Integer sortOrder,
    @NotEmpty @Valid List<OptionRequest> options
) {}
