package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.UserWarn;
import store.onuljang.repository.entity.enums.UserWarnReason;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
public record AdminCustomerWarnResponse(
    List<CustomerWarnResponse> response
) {

    public static AdminCustomerWarnResponse of(List<UserWarn> warn) {
        return AdminCustomerWarnResponse.builder()
            .response(new ArrayList<>(
                warn.stream()
                    .map(CustomerWarnResponse::of)
                    .toList()))
            .build();
    }

    @Builder
    public record CustomerWarnResponse(
        UserWarnReason reason,
        LocalDateTime warnAt
    ) {
        public static CustomerWarnResponse of(UserWarn warn) {
            return CustomerWarnResponse.builder()
                .reason(warn.getReason())
                .warnAt(warn.getCreatedAt())
                .build();
        }
    }
}