package store.onuljang.courier.dto;

import java.util.List;
import org.springframework.data.domain.Page;
import store.onuljang.courier.entity.CourierOrder;

public record AdminCourierOrderListResponse(
        List<AdminCourierOrderResponse> orders,
        int totalPages,
        long totalElements,
        int currentPage) {
    public static AdminCourierOrderListResponse from(Page<CourierOrder> page) {
        return new AdminCourierOrderListResponse(
                page.getContent().stream().map(AdminCourierOrderResponse::from).toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber());
    }
}
