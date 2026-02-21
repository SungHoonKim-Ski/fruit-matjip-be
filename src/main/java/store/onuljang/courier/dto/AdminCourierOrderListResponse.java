package store.onuljang.courier.dto;

import java.util.List;
import store.onuljang.courier.entity.CourierOrder;

public record AdminCourierOrderListResponse(List<AdminCourierOrderResponse> orders) {
    public static AdminCourierOrderListResponse from(List<CourierOrder> orders) {
        return new AdminCourierOrderListResponse(
                orders.stream().map(AdminCourierOrderResponse::from).toList());
    }
}
