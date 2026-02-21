package store.onuljang.shop.delivery.dto;

import lombok.Builder;
import store.onuljang.shared.user.entity.UserDeliveryInfo;

@Builder
public record DeliveryInfoResponse(
    String phone,
    String postalCode,
    String address1,
    String address2,
    Double latitude,
    Double longitude
) {
    public static DeliveryInfoResponse from(UserDeliveryInfo info) {
        return DeliveryInfoResponse.builder()
            .phone(info.getPhone())
            .postalCode(info.getPostalCode())
            .address1(info.getAddress1())
            .address2(info.getAddress2())
            .latitude(info.getLatitude())
            .longitude(info.getLongitude())
            .build();
    }
}
