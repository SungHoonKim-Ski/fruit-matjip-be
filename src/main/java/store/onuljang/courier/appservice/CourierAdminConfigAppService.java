package store.onuljang.courier.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.dto.CourierConfigAdminResponse;
import store.onuljang.courier.dto.CourierConfigUpdateRequest;
import store.onuljang.courier.entity.CourierConfig;
import store.onuljang.courier.service.CourierConfigService;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierAdminConfigAppService {

    CourierConfigService courierConfigService;

    public CourierConfigAdminResponse getConfig() {
        CourierConfig config = courierConfigService.getConfig();
        return CourierConfigAdminResponse.from(config);
    }

    @Transactional
    public CourierConfigAdminResponse updateConfig(CourierConfigUpdateRequest request) {
        CourierConfig config = courierConfigService.getConfig();
        config.update(
                request.enabled() != null ? request.enabled() : config.isEnabled(),
                request.islandSurcharge() != null
                        ? request.islandSurcharge()
                        : config.getIslandSurcharge(),
                request.noticeText() != null ? request.noticeText() : config.getNoticeText(),
                request.senderName() != null ? request.senderName() : config.getSenderName(),
                request.senderPhone() != null ? request.senderPhone() : config.getSenderPhone(),
                request.senderPhone2() != null ? request.senderPhone2() : config.getSenderPhone2(),
                request.senderAddress() != null
                        ? request.senderAddress()
                        : config.getSenderAddress(),
                request.senderDetailAddress() != null
                        ? request.senderDetailAddress()
                        : config.getSenderDetailAddress());
        courierConfigService.updateConfig(config);
        return CourierConfigAdminResponse.from(config);
    }
}
