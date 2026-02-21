package store.onuljang.courier.dto;

import java.util.List;
import store.onuljang.courier.entity.CourierProduct;

public record CourierProductListResponse(List<CourierProductResponse> response) {

    public static CourierProductListResponse from(List<CourierProduct> products) {
        return new CourierProductListResponse(
                products.stream().map(CourierProductResponse::from).toList());
    }
}
