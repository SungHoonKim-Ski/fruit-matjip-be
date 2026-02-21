package store.onuljang.courier.dto;

import java.math.BigDecimal;
import store.onuljang.courier.entity.CourierConfig;

public record CourierConfigAdminResponse(
        Long id,
        boolean enabled,
        BigDecimal islandSurcharge,
        String noticeText,
        String senderName,
        String senderPhone,
        String senderPhone2,
        String senderAddress,
        String senderDetailAddress) {

    public static CourierConfigAdminResponse from(CourierConfig config) {
        return new CourierConfigAdminResponse(
                config.getId(),
                config.isEnabled(),
                config.getIslandSurcharge(),
                config.getNoticeText(),
                config.getSenderName(),
                config.getSenderPhone(),
                config.getSenderPhone2(),
                config.getSenderAddress(),
                config.getSenderDetailAddress());
    }
}
