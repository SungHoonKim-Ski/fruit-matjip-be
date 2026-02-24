package store.onuljang.courier.appservice;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.dto.AdminCourierOrderDetailResponse;
import store.onuljang.courier.dto.AdminCourierOrderListResponse;
import store.onuljang.courier.dto.WaybillExcelFilterRequest;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import org.springframework.web.multipart.MultipartFile;
import store.onuljang.courier.dto.TrackingUploadResponse;
import store.onuljang.courier.service.CourierOrderService;
import store.onuljang.courier.service.CourierPaymentService;
import store.onuljang.courier.service.CourierRefundService;
import store.onuljang.courier.service.TrackingUploadService;
import store.onuljang.courier.service.WaybillExcelService;
import store.onuljang.shared.entity.enums.CourierCompany;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.entity.enums.UserPointTransactionType;
import store.onuljang.shared.exception.AdminValidateException;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.user.service.UserPointService;

@Slf4j
@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierAdminOrderAppService {

    CourierOrderService courierOrderService;
    CourierPaymentService courierPaymentService;
    CourierRefundService courierRefundService;
    WaybillExcelService waybillExcelService;
    TrackingUploadService trackingUploadService;
    UserPointService userPointService;

    public AdminCourierOrderListResponse getOrders(
            CourierOrderStatus status, Boolean waybillDownloaded, int page, int size) {
        org.springframework.data.domain.Page<CourierOrder> orders;
        if (waybillDownloaded != null) {
            orders = courierOrderService.findAllByStatusAndWaybillDownloaded(
                    status, waybillDownloaded, page, size);
        } else {
            orders = courierOrderService.findAllByStatus(status, page, size);
        }
        return AdminCourierOrderListResponse.from(orders);
    }

    public AdminCourierOrderDetailResponse getOrder(Long id) {
        CourierOrder order = courierOrderService.findByIdWithItems(id);
        return AdminCourierOrderDetailResponse.from(order);
    }

    @Transactional
    public void updateStatus(Long id, CourierOrderStatus nextStatus) {
        CourierOrder order = courierOrderService.findById(id);
        validateAdminTransition(order, nextStatus);
        applyStatus(order, nextStatus);
    }

    @Transactional
    public void ship(Long id, String waybillNumber) {
        CourierOrder order = courierOrderService.findById(id);
        if (order.getStatus() != CourierOrderStatus.PAID
                && order.getStatus() != CourierOrderStatus.PREPARING) {
            throw new AdminValidateException("발송 처리는 결제완료 또는 준비중 상태에서만 가능합니다.");
        }
        order.markShipped(waybillNumber);
    }

    @Transactional
    public void cancel(Long id) {
        CourierOrder order = courierOrderService.findByIdWithItems(id);
        if (order.getStatus() == CourierOrderStatus.SHIPPED
                || order.getStatus() == CourierOrderStatus.DELIVERED
                || order.getStatus() == CourierOrderStatus.CANCELED) {
            throw new AdminValidateException("이미 발송/배송완료/취소된 주문은 취소할 수 없습니다.");
        }

        if (order.getStatus() == CourierOrderStatus.PAID
                || order.getStatus() == CourierOrderStatus.PREPARING) {
            if (order.getPgPaymentAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                try {
                    courierRefundService.refund(order, order.getPgPaymentAmount());
                } catch (Exception e) {
                    log.warn("환불 실패 (orderId={}): {}", order.getId(), e.getMessage());
                }
            }
            if (order.getPointUsed().compareTo(java.math.BigDecimal.ZERO) > 0) {
                userPointService.earn(
                        order.getUser().getUid(), order.getPointUsed(),
                        UserPointTransactionType.CANCEL_USE,
                        "관리자취소#" + order.getDisplayCode() + " 포인트 환원",
                        "COURIER_ORDER", order.getId(), "admin");
            }
        }

        order.markCanceled();
        courierPaymentService.markCanceled(order);
        restoreStock(order);
    }

    @Transactional
    public TrackingUploadResponse uploadTracking(MultipartFile file, CourierCompany courierCompany) {
        return trackingUploadService.uploadTracking(file, courierCompany);
    }

    @Transactional
    public byte[] downloadWaybillExcel(Long orderId) {
        CourierOrder order = courierOrderService.findByIdWithItems(orderId);
        order.markWaybillDownloaded();
        return waybillExcelService.generateWaybillExcel(order);
    }

    @Transactional
    public byte[] downloadWaybillExcelBulk(List<Long> orderIds) {
        List<CourierOrder> orders = courierOrderService.findAllByIds(orderIds);
        orders.forEach(CourierOrder::markWaybillDownloaded);
        return waybillExcelService.generateWaybillExcel(orders);
    }

    @Transactional
    public byte[] downloadWaybillExcelByFilter(WaybillExcelFilterRequest request) {
        request.validate();
        LocalDateTime startDateTime = request.startDate().atStartOfDay();
        LocalDateTime endDateTime = request.endDate().plusDays(1).atStartOfDay();

        List<CourierOrderStatus> statuses = List.of(
                CourierOrderStatus.PAID,
                CourierOrderStatus.SHIPPED,
                CourierOrderStatus.DELIVERED);

        List<CourierOrder> orders;
        if (request.productId() != null) {
            orders = courierOrderService.findByDateRangeAndStatusesAndProduct(
                    startDateTime, endDateTime, statuses, request.productId());
        } else {
            orders = courierOrderService.findByDateRangeAndStatuses(
                    startDateTime, endDateTime, statuses);
        }

        if (orders.isEmpty()) {
            throw new UserValidateException("해당 조건에 맞는 주문이 없습니다.");
        }

        orders.forEach(CourierOrder::markWaybillDownloaded);
        return waybillExcelService.generateWaybillExcel(orders);
    }

    private void validateAdminTransition(CourierOrder order, CourierOrderStatus nextStatus) {
        CourierOrderStatus current = order.getStatus();
        switch (nextStatus) {
            case PREPARING -> {
                if (current != CourierOrderStatus.PAID) {
                    throw new AdminValidateException("준비중은 결제완료 상태에서만 가능합니다.");
                }
            }
            case SHIPPED -> {
                if (current != CourierOrderStatus.PAID
                        && current != CourierOrderStatus.PREPARING) {
                    throw new AdminValidateException(
                            "발송완료는 결제완료 또는 준비중 상태에서만 가능합니다.");
                }
            }
            case IN_TRANSIT -> {
                if (current != CourierOrderStatus.SHIPPED) {
                    throw new AdminValidateException("배송중은 발송완료 상태에서만 가능합니다.");
                }
            }
            case DELIVERED -> {
                if (current != CourierOrderStatus.SHIPPED
                        && current != CourierOrderStatus.IN_TRANSIT) {
                    throw new AdminValidateException(
                            "배송완료는 발송완료 또는 배송중 상태에서만 가능합니다.");
                }
            }
            default -> throw new AdminValidateException("변경할 수 없는 상태입니다: " + nextStatus);
        }
    }

    private void applyStatus(CourierOrder order, CourierOrderStatus nextStatus) {
        switch (nextStatus) {
            case PREPARING -> order.markPreparing();
            case SHIPPED -> order.markShipped(order.getWaybillNumber());
            case IN_TRANSIT -> order.markInTransit();
            case DELIVERED -> order.markDelivered();
            default -> throw new AdminValidateException("변경할 수 없는 상태입니다.");
        }
    }

    private void restoreStock(CourierOrder order) {
        for (CourierOrderItem item : order.getItems()) {
            if (item.getCourierProduct() != null) {
                item.getCourierProduct().restoreStock(item.getQuantity());
            }
            if (item.getSelectedOptionIds() != null
                    && !item.getSelectedOptionIds().isBlank()
                    && item.getCourierProduct() != null) {
                java.util.Set<Long> optIds = java.util.Arrays.stream(item.getSelectedOptionIds().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .collect(java.util.stream.Collectors.toSet());
                for (store.onuljang.courier.entity.CourierProductOptionGroup group : item.getCourierProduct().getOptionGroups()) {
                    for (store.onuljang.courier.entity.CourierProductOption opt : group.getOptions()) {
                        if (optIds.contains(opt.getId())) {
                            opt.restoreStock(item.getQuantity());
                        }
                    }
                }
            }
        }
    }
}
