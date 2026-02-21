package store.onuljang.shop.delivery.dto;

import lombok.Builder;

@Builder
public record DeliveryReadyResponse(
    String orderCode,
    String redirectUrl,
    String mobileRedirectUrl
) {}
