package store.onuljang.courier.dto;

import jakarta.validation.constraints.NotBlank;
import store.onuljang.shared.entity.enums.CourierCompany;

public record CourierShipRequest(
        @NotBlank String waybillNumber, CourierCompany courierCompany) {

    public CourierShipRequest {
        if (courierCompany == null) {
            courierCompany = CourierCompany.LOGEN;
        }
    }
}
