package store.onuljang.controller.response;

import lombok.Builder;

@Builder
public record DeliveryReadyResponse(
    long orderId,
    String redirectUrl,
    String mobileRedirectUrl
) {}
