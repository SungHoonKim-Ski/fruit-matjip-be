package store.onuljang.courier.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import store.onuljang.shared.user.entity.UserCourierInfo;

@Builder
public record CourierInfoResponse(
    @JsonProperty("receiver_name") String receiverName,
    @JsonProperty("receiver_phone") String receiverPhone,
    @JsonProperty("postal_code") String postalCode,
    String address1,
    String address2
) {
    public static CourierInfoResponse from(UserCourierInfo info) {
        return CourierInfoResponse.builder()
            .receiverName(info.getReceiverName())
            .receiverPhone(info.getReceiverPhone())
            .postalCode(info.getPostalCode())
            .address1(info.getAddress1())
            .address2(info.getAddress2())
            .build();
    }
}
