package store.onuljang.courier.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import store.onuljang.shared.entity.enums.CourierOrderStatus;

public record WaybillExcelFilterRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        Long productId,
        CourierOrderStatus status) {

    public void validate() {
        if (endDate.isBefore(startDate)) {
            throw new store.onuljang.shared.exception.UserValidateException("종료일은 시작일 이후여야 합니다.");
        }
        if (startDate.plusDays(365).isBefore(endDate)) {
            throw new store.onuljang.shared.exception.UserValidateException("조회 기간은 최대 365일입니다.");
        }
    }
}
