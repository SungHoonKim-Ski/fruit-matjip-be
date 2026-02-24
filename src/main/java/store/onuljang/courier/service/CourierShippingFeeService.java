package store.onuljang.courier.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.dto.ShippingFeeItemInput;
import store.onuljang.courier.dto.ShippingFeePreviewRequest;
import store.onuljang.courier.dto.ShippingFeeResult;
import store.onuljang.courier.entity.CourierConfig;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.entity.ShippingFeeTemplate;
import store.onuljang.courier.repository.ShippingFeeTemplateRepository;
import store.onuljang.shared.exception.UserValidateException;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierShippingFeeService {

    ShippingFeeTemplateRepository shippingFeeTemplateRepository;
    CourierConfigService courierConfigService;
    CourierProductService courierProductService;

    public ShippingFeeResult calculatePreview(ShippingFeePreviewRequest request) {
        List<ShippingFeeItemInput> feeItems =
                request.items().stream()
                        .map(item -> {
                            CourierProduct product =
                                    courierProductService.findById(item.courierProductId());
                            BigDecimal unitPrice = product.getPrice();
                            BigDecimal itemAmount =
                                    unitPrice.multiply(BigDecimal.valueOf(item.quantity()));
                            Long templateId =
                                    product.getShippingFeeTemplate() != null
                                            ? product.getShippingFeeTemplate().getId()
                                            : null;
                            return new ShippingFeeItemInput(
                                    product.getId(),
                                    item.quantity(),
                                    itemAmount,
                                    templateId,
                                    product.getCombinedShippingFee());
                        })
                        .toList();
        return calculateByItems(feeItems, request.postalCode());
    }

    /**
     * 상품별 배송비 계산 (템플릿 기반) 같은 템플릿 상품끼리 그룹핑하여 계산
     */
    public ShippingFeeResult calculateByItems(
            List<ShippingFeeItemInput> items, String postalCode) {

        CourierConfig config = courierConfigService.getConfig();
        BigDecimal totalShippingFee = BigDecimal.ZERO;

        // 템플릿별 그룹핑
        Map<Long, List<ShippingFeeItemInput>> templateGroups = new HashMap<>();
        List<ShippingFeeItemInput> globalPolicyItems = new ArrayList<>();

        for (ShippingFeeItemInput item : items) {
            if (item.templateId() != null) {
                templateGroups
                        .computeIfAbsent(item.templateId(), k -> new ArrayList<>())
                        .add(item);
            } else {
                globalPolicyItems.add(item);
            }
        }

        // 1) 템플릿별 배송비 계산
        for (Map.Entry<Long, List<ShippingFeeItemInput>> entry : templateGroups.entrySet()) {
            ShippingFeeTemplate template =
                    shippingFeeTemplateRepository
                            .findById(entry.getKey())
                            .orElseThrow(
                                    () -> new UserValidateException("배송비 템플릿을 찾을 수 없습니다."));

            int groupQuantity =
                    entry.getValue().stream().mapToInt(ShippingFeeItemInput::quantity).sum();
            BigDecimal groupAmount =
                    entry.getValue().stream()
                            .map(ShippingFeeItemInput::itemAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalShippingFee = totalShippingFee.add(template.calculateFee(groupQuantity, groupAmount));
        }

        // 2) 기본배송 상품 (템플릿 없는 상품)
        for (ShippingFeeItemInput item : globalPolicyItems) {
            BigDecimal feePerItem;
            if (item.combinedShippingFee() != null) {
                // 합배송 금액이 설정된 경우: 수량 × 합배송 단가
                feePerItem = item.combinedShippingFee().multiply(BigDecimal.valueOf(item.quantity()));
            } else {
                // 합배송 금액 미설정: 수량 × 기본 배송비
                feePerItem = config.getBaseShippingFee().multiply(BigDecimal.valueOf(item.quantity()));
            }
            totalShippingFee = totalShippingFee.add(feePerItem);
        }

        // 3) 도서산간 추가
        boolean isIsland = isIslandPostalCode(postalCode);
        BigDecimal islandSurcharge = BigDecimal.ZERO;
        if (isIsland) {
            islandSurcharge = config.getIslandSurcharge();
        }

        BigDecimal finalTotal = totalShippingFee.add(islandSurcharge);
        return new ShippingFeeResult(totalShippingFee, islandSurcharge, isIsland, finalTotal);
    }

    public boolean isIslandPostalCode(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            return false;
        }
        try {
            int code = Integer.parseInt(postalCode.replaceAll("[^0-9]", ""));
            return code >= 63000 && code <= 63644;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
