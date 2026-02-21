package store.onuljang.shop.admin.dto;

import jakarta.validation.constraints.*;
import store.onuljang.shared.entity.enums.ReservationStatus;

import java.util.Set;

public record AdminUpdateReservationsRequest(
    @NotEmpty
    Set<@NotNull Long> reservationIds,

    @NotNull ReservationStatus status
) {

}
