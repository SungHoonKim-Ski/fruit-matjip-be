package store.onuljang.controller.response;

import lombok.Builder;

@Builder
public record DeliveryReadyResponse(
    String orderCode,
    String redirectUrl,
    String mobileRedirectUrl
) {}
