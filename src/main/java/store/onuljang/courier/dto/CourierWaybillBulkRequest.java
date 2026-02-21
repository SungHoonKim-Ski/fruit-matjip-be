package store.onuljang.courier.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CourierWaybillBulkRequest(@NotEmpty List<Long> orderIds) {}
