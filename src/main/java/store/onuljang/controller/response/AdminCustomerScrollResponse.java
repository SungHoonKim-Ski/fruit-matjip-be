package store.onuljang.controller.response;

import store.onuljang.repository.entity.Users;
import store.onuljang.util.Pagination;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AdminCustomerScrollResponse(
    List<CustomerListItemResponse> response,
    Pagination pagination
) {
    public static AdminCustomerScrollResponse of(
        List<Users> users,
        boolean hasNext,
        String nextCursor
    ) {
        List<CustomerListItemResponse> list = users.stream()
            .map(user -> new CustomerListItemResponse(
                user.getUid(),
                user.getName(),
                user.getTotalRevenue(),
                user.getMonthlyWarnCount(),
                user.getTotalWarnCount(),
                user.getLastOrderDate() == null,
                user.getRestrictedUntil()
            ))
            .toList();

        Pagination pagination = new Pagination(hasNext, nextCursor);

        return new AdminCustomerScrollResponse(list, pagination);
    }

    public record CustomerListItemResponse(
        String uid,
        String name,
        BigDecimal totalRevenue,
        int monthlyWarnCount,
        int totalWarnCount,
        boolean firstTimeBuyer,
        LocalDate restrictedUntil
    ) {}
}
