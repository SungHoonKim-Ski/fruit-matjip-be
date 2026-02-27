package store.onuljang.shop.admin.dto;

import lombok.Builder;
import store.onuljang.shop.delivery.entity.DeliveryDailyAggRow;
import store.onuljang.shop.product.entity.ProductDailyAggRow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Builder
public record AdminReservationSummaryResponse(List<AdminSummaryResponse> summary) {
    @Builder
    public record AdminSummaryResponse(
            LocalDate date,
            BigDecimal amount,
            int quantity,
            BigDecimal deliveryAmount,
            BigDecimal deliveryFee,
            int deliveryOrderCount) {}

    public static AdminReservationSummaryResponse from(
            List<ProductDailyAggRow> productRows, List<DeliveryDailyAggRow> deliveryRows) {

        Map<LocalDate, DeliveryDailyAggRow> deliveryMap =
                deliveryRows.stream()
                        .collect(Collectors.toMap(DeliveryDailyAggRow::getSellDate, r -> r));

        List<AdminSummaryResponse> summaryList =
                productRows.stream()
                        .map(
                                p -> {
                                    DeliveryDailyAggRow d = deliveryMap.get(p.getSellDate());
                                    return AdminSummaryResponse.builder()
                                            .date(p.getSellDate())
                                            .amount(p.getAmount())
                                            .quantity(p.getQuantity())
                                            .deliveryAmount(
                                                    d != null
                                                            ? d.getAmount()
                                                            : BigDecimal.ZERO)
                                            .deliveryFee(
                                                    d != null
                                                            ? d.getDeliveryFee()
                                                            : BigDecimal.ZERO)
                                            .deliveryOrderCount(
                                                    d != null ? d.getOrderCount() : 0)
                                            .build();
                                })
                        .toList();

        Set<LocalDate> productDates =
                productRows.stream()
                        .map(ProductDailyAggRow::getSellDate)
                        .collect(Collectors.toSet());

        List<AdminSummaryResponse> deliveryOnlyList =
                deliveryRows.stream()
                        .filter(d -> !productDates.contains(d.getSellDate()))
                        .map(
                                d ->
                                        AdminSummaryResponse.builder()
                                                .date(d.getSellDate())
                                                .amount(BigDecimal.ZERO)
                                                .quantity(0)
                                                .deliveryAmount(d.getAmount())
                                                .deliveryFee(d.getDeliveryFee())
                                                .deliveryOrderCount(d.getOrderCount())
                                                .build())
                        .toList();

        List<AdminSummaryResponse> merged = new ArrayList<>(summaryList);
        merged.addAll(deliveryOnlyList);
        merged.sort(Comparator.comparing(AdminSummaryResponse::date));

        return AdminReservationSummaryResponse.builder().summary(merged).build();
    }
}
