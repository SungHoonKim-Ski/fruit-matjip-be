package store.onuljang.courier.dto;

import java.math.BigDecimal;
import store.onuljang.courier.entity.CourierConfig;

public record CourierConfigResponse(
        boolean enabled, BigDecimal islandSurcharge, String noticeText) {

    public static CourierConfigResponse from(CourierConfig config) {
        return new CourierConfigResponse(
                config.isEnabled(), config.getIslandSurcharge(), config.getNoticeText());
    }
}
