package store.onuljang.controller.request;

import jakarta.validation.constraints.*;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.util.Set;

public record AdminUpdateReservationsRequest(
    @NotEmpty
    Set<@NotNull Long> reservationIds,

    @NotNull ReservationStatus status
) {

}