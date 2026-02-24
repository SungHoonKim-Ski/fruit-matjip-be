package store.onuljang.courier.dto;

import java.math.BigDecimal;

public record CourierConfigUpdateRequest(
        Boolean enabled,
        BigDecimal islandSurcharge,
        BigDecimal baseShippingFee,
        String noticeText,
        String senderName,
        String senderPhone,
        String senderPhone2,
        String senderAddress,
        String senderDetailAddress) {}
