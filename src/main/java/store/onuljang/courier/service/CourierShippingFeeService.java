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
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.entity.ShippingFeePolicy;
import store.onuljang.courier.entity.ShippingFeeTemplate;
import store.onuljang.courier.repository.ShippingFeePolicyRepository;
import store.onuljang.courier.repository.ShippingFeeTemplateRepository;
import store.onuljang.shared.exception.UserValidateException;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierShippingFeeService {

    ShippingFeePolicyRepository shippingFeePolicyRepository;
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
                                    product.getId(), item.quantity(), itemAmount, templateId);
                        })
                        .toList();
        return calculateByItems(feeItems, request.postalCode());
    }

    public ShippingFeeResult calculate(int totalQuantity, String postalCode) {
        ShippingFeePolicy policy =
                shippingFeePolicyRepository.findAllByQuantityRange(totalQuantity).stream()
                        .findFirst()
                        .orElseThrow(
                                () -> new UserValidateException("해당 수량에 대한 배송비 정책이 없습니다."));

        BigDecimal shippingFee = policy.getFee();
        boolean isIsland = isIslandPostalCode(postalCode);
        BigDecimal islandSurcharge = BigDecimal.ZERO;

        if (isIsland) {
            islandSurcharge = courierConfigService.getConfig().getIslandSurcharge();
        }

        BigDecimal totalShippingFee = shippingFee.add(islandSurcharge);

        return new ShippingFeeResult(shippingFee, islandSurcharge, isIsland, totalShippingFee);
    }

    /**
     * 상품별 배송비 계산 (템플릿 기반) 같은 템플릿 상품끼리 그룹핑하여 계산
     */
    public ShippingFeeResult calculateByItems(
            List<ShippingFeeItemInput> items, String postalCode) {

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

        // 2) 전역 정책 상품 배송비 계산
        if (!globalPolicyItems.isEmpty()) {
            int policyQuantity =
                    globalPolicyItems.stream().mapToInt(ShippingFeeItemInput::quantity).sum();
            ShippingFeePolicy policy =
                    shippingFeePolicyRepository.findAllByQuantityRange(policyQuantity).stream()
                            .findFirst()
                            .orElseThrow(
                                    () -> new UserValidateException("해당 수량에 대한 배송비 정책이 없습니다."));
            totalShippingFee = totalShippingFee.add(policy.getFee());
        }

        // 3) 도서산간 추가
        boolean isIsland = isIslandPostalCode(postalCode);
        BigDecimal islandSurcharge = BigDecimal.ZERO;
        if (isIsland) {
            islandSurcharge = courierConfigService.getConfig().getIslandSurcharge();
        }

        BigDecimal finalTotal = totalShippingFee.add(islandSurcharge);
        return new ShippingFeeResult(totalShippingFee, islandSurcharge, isIsland, finalTotal);
    }

    public List<ShippingFeePolicy> findAll() {
        return shippingFeePolicyRepository.findAllByOrderBySortOrderAsc();
    }

    @Transactional
    public List<ShippingFeePolicy> replaceAll(List<ShippingFeePolicy> policies) {
        validateNoOverlap(policies);
        shippingFeePolicyRepository.deleteAllInBatch();
        return shippingFeePolicyRepository.saveAll(policies);
    }

    private void validateNoOverlap(List<ShippingFeePolicy> policies) {
        List<ShippingFeePolicy> activePolicies =
                policies.stream().filter(ShippingFeePolicy::getActive).toList();
        for (int i = 0; i < activePolicies.size(); i++) {
            for (int j = i + 1; j < activePolicies.size(); j++) {
                ShippingFeePolicy a = activePolicies.get(i);
                ShippingFeePolicy b = activePolicies.get(j);
                if (a.getMinQuantity() <= b.getMaxQuantity()
                        && b.getMinQuantity() <= a.getMaxQuantity()) {
                    throw new UserValidateException(
                            String.format(
                                    "수량 범위가 겹칩니다: [%d-%d]과 [%d-%d]",
                                    a.getMinQuantity(),
                                    a.getMaxQuantity(),
                                    b.getMinQuantity(),
                                    b.getMaxQuantity()));
                }
            }
        }
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
