package store.onuljang.courier.service;

import java.math.BigDecimal;
import java.util.List;
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

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierShippingFeeService {

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
                            return new ShippingFeeItemInput(
                                    product.getId(),
                                    item.quantity(),
                                    itemAmount,
                                    product.getShippingFee(),
                                    product.getCombinedShippingQuantity());
                        })
                        .toList();
        return calculateByItems(feeItems, request.postalCode());
    }

    /**
     * 상품별 배송비 계산 (합배송 수량 기반)
     * 공식: ceil(수량 / 합배송수량) × 상품배송비
     */
    public ShippingFeeResult calculateByItems(
            List<ShippingFeeItemInput> items, String postalCode) {

        CourierConfig config = courierConfigService.getConfig();
        BigDecimal totalShippingFee = BigDecimal.ZERO;

        for (ShippingFeeItemInput item : items) {
            int shippingUnits = (int) Math.ceil(
                    (double) item.quantity() / item.combinedShippingQuantity());
            totalShippingFee = totalShippingFee.add(
                    item.shippingFee().multiply(BigDecimal.valueOf(shippingUnits)));
        }

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
