package store.onuljang.courier.appservice;

import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.dto.CourierConfigAdminResponse;
import store.onuljang.courier.dto.CourierConfigUpdateRequest;
import store.onuljang.courier.dto.ShippingFeePolicyListResponse;
import store.onuljang.courier.dto.ShippingFeePolicyRequest;
import store.onuljang.courier.dto.ShippingFeeTemplateListResponse;
import store.onuljang.courier.dto.ShippingFeeTemplateRequest;
import store.onuljang.courier.dto.ShippingFeeTemplateResponse;
import store.onuljang.courier.entity.CourierConfig;
import store.onuljang.courier.entity.ShippingFeePolicy;
import store.onuljang.courier.entity.ShippingFeeTemplate;
import store.onuljang.courier.service.CourierConfigService;
import store.onuljang.courier.service.CourierShippingFeeService;
import store.onuljang.courier.service.ShippingFeeTemplateService;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierAdminConfigAppService {

    CourierConfigService courierConfigService;
    CourierShippingFeeService courierShippingFeeService;
    ShippingFeeTemplateService shippingFeeTemplateService;

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

    public ShippingFeePolicyListResponse getShippingFeePolicies() {
        List<ShippingFeePolicy> policies = courierShippingFeeService.findAll();
        return ShippingFeePolicyListResponse.from(policies);
    }

    @Transactional
    public ShippingFeePolicyListResponse replaceShippingFeePolicies(
            List<ShippingFeePolicyRequest> requests) {
        List<ShippingFeePolicy> policies =
                requests.stream()
                        .map(
                                req ->
                                        ShippingFeePolicy.builder()
                                                .minQuantity(req.minQuantity())
                                                .maxQuantity(req.maxQuantity())
                                                .fee(req.fee())
                                                .sortOrder(
                                                        req.sortOrder() != null ? req.sortOrder() : 0)
                                                .build())
                        .toList();
        List<ShippingFeePolicy> saved = courierShippingFeeService.replaceAll(policies);
        return ShippingFeePolicyListResponse.from(saved);
    }

    public ShippingFeeTemplateListResponse getShippingFeeTemplates() {
        return ShippingFeeTemplateListResponse.from(shippingFeeTemplateService.findAll());
    }

    @Transactional
    public ShippingFeeTemplateResponse createShippingFeeTemplate(
            ShippingFeeTemplateRequest request) {
        ShippingFeeTemplate template =
                ShippingFeeTemplate.builder()
                        .name(request.name())
                        .baseFee(request.baseFee())
                        .perQuantityFee(request.perQuantityFee())
                        .freeShippingMinAmount(request.freeShippingMinAmount())
                        .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                        .build();
        return ShippingFeeTemplateResponse.from(shippingFeeTemplateService.save(template));
    }

    @Transactional
    public ShippingFeeTemplateResponse updateShippingFeeTemplate(
            Long id, ShippingFeeTemplateRequest request) {
        ShippingFeeTemplate template = shippingFeeTemplateService.findById(id);
        template.setName(request.name());
        template.setBaseFee(request.baseFee());
        template.setPerQuantityFee(request.perQuantityFee());
        template.setFreeShippingMinAmount(request.freeShippingMinAmount());
        if (request.sortOrder() != null) template.setSortOrder(request.sortOrder());
        return ShippingFeeTemplateResponse.from(template);
    }

    @Transactional
    public void deleteShippingFeeTemplate(Long id) {
        shippingFeeTemplateService.delete(id);
    }
}
