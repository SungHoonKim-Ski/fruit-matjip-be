package store.onuljang.courier.dto;

import lombok.Builder;

@Builder
public record CourierOrderReadyResponse(
        String displayCode, String redirectUrl, String mobileRedirectUrl) {}
