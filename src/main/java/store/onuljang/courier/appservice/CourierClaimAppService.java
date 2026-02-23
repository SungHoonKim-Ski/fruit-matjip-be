package store.onuljang.courier.appservice;

import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.dto.CourierClaimApproveRequest;
import store.onuljang.courier.dto.CourierClaimListResponse;
import store.onuljang.courier.dto.CourierClaimRequest;
import store.onuljang.courier.dto.CourierClaimResponse;
import store.onuljang.courier.entity.CourierClaim;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.courier.service.CourierClaimService;
import store.onuljang.courier.service.CourierOrderService;
import store.onuljang.courier.service.CourierRefundService;
import store.onuljang.shared.entity.enums.CourierClaimStatus;
import store.onuljang.shared.entity.enums.CourierClaimType;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.entity.enums.ShippingFeeBearer;
import store.onuljang.shared.exception.AdminValidateException;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.user.service.UserService;

@Slf4j
@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierClaimAppService {

    UserService userService;
    CourierOrderService courierOrderService;
    CourierClaimService courierClaimService;
    CourierRefundService courierRefundService;

    // === 사용자: 클레임 접수 ===

    @Transactional
    public CourierClaimResponse createClaim(
            String uid, String displayCode, CourierClaimRequest request) {
        Users user = userService.findByUId(uid);
        CourierOrder order = courierOrderService.findByDisplayCodeAndUser(displayCode, user);

        if (order.getStatus() == CourierOrderStatus.PENDING_PAYMENT
                || order.getStatus() == CourierOrderStatus.CANCELED
                || order.getStatus() == CourierOrderStatus.FAILED) {
            throw new UserValidateException("결제 완료 후 문의가 가능합니다.");
        }

        CourierOrderItem targetItem = null;
        if (request.courierOrderItemId() != null) {
            targetItem =
                    order.getItems().stream()
                            .filter(i -> i.getCourierProduct() != null
                                    && i.getCourierProduct().getId().equals(request.courierOrderItemId()))
                            .findFirst()
                            .orElseThrow(() -> new UserValidateException("주문에 해당 상품이 없습니다."));
            targetItem.markClaimRequested();
        }

        ShippingFeeBearer bearer =
                request.claimType() == CourierClaimType.QUALITY_ISSUE
                        ? ShippingFeeBearer.SELLER
                        : ShippingFeeBearer.CUSTOMER;

        CourierClaim claim =
                CourierClaim.builder()
                        .courierOrder(order)
                        .courierOrderItem(targetItem)
                        .claimType(request.claimType())
                        .claimStatus(CourierClaimStatus.REQUESTED)
                        .reason(request.reason())
                        .returnShippingFeeBearer(bearer)
                        .build();

        CourierClaim saved = courierClaimService.save(claim);
        return CourierClaimResponse.from(saved);
    }

    // === 사용자: 내 클레임 조회 ===

    public List<CourierClaimResponse> getClaimsByOrder(String uid, String displayCode) {
        Users user = userService.findByUId(uid);
        CourierOrder order = courierOrderService.findByDisplayCodeAndUser(displayCode, user);
        return courierClaimService.findByOrder(order).stream()
                .map(CourierClaimResponse::from)
                .toList();
    }

    // === 관리자: 클레임 목록 ===

    public CourierClaimListResponse getAdminClaims(
            CourierClaimStatus status, int page, int size) {
        List<CourierClaim> claims = courierClaimService.findAllByStatus(status, page, size);
        return CourierClaimListResponse.from(claims);
    }

    // === 관리자: 클레임 상세 ===

    public CourierClaimResponse getAdminClaim(Long claimId) {
        CourierClaim claim = courierClaimService.findById(claimId);
        return CourierClaimResponse.from(claim);
    }

    // === 관리자: 클레임 승인 ===

    @Transactional
    public CourierClaimResponse approveClaim(Long claimId, CourierClaimApproveRequest request) {
        CourierClaim claim = courierClaimService.findById(claimId);
        if (claim.getClaimStatus() != CourierClaimStatus.REQUESTED
                && claim.getClaimStatus() != CourierClaimStatus.IN_REVIEW) {
            throw new AdminValidateException("승인 가능한 상태가 아닙니다.");
        }

        CourierOrder order = claim.getCourierOrder();
        BigDecimal refundAmount =
                request.refundAmount() != null
                        ? request.refundAmount()
                        : (claim.getCourierOrderItem() != null
                                ? claim.getCourierOrderItem().getAmount()
                                : order.getTotalAmount());

        ShippingFeeBearer bearer =
                claim.getClaimType() == CourierClaimType.QUALITY_ISSUE
                        ? ShippingFeeBearer.SELLER
                        : ShippingFeeBearer.CUSTOMER;

        claim.approve(request.adminNote(), refundAmount, bearer);

        if ("REFUND".equalsIgnoreCase(request.action())) {
            courierRefundService.refund(order, refundAmount);
            if (claim.getCourierOrderItem() != null) {
                claim.getCourierOrderItem().markRefunded();
            }
            claim.resolve();
        }

        return CourierClaimResponse.from(claim);
    }

    // === 관리자: 클레임 거부 ===

    @Transactional
    public CourierClaimResponse rejectClaim(Long claimId, String adminNote) {
        CourierClaim claim = courierClaimService.findById(claimId);
        if (claim.getClaimStatus() != CourierClaimStatus.REQUESTED
                && claim.getClaimStatus() != CourierClaimStatus.IN_REVIEW) {
            throw new AdminValidateException("거부 가능한 상태가 아닙니다.");
        }

        claim.reject(adminNote);

        if (claim.getCourierOrderItem() != null) {
            claim.getCourierOrderItem().markClaimResolved();
        }

        return CourierClaimResponse.from(claim);
    }
}
