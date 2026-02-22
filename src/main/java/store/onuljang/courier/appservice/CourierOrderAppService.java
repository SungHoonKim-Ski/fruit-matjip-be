package store.onuljang.courier.appservice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.dto.*;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.entity.CourierProductOption;
import store.onuljang.courier.entity.CourierProductOptionGroup;
import store.onuljang.courier.service.*;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.entity.enums.PaymentProvider;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.feign.dto.request.KakaoPayApproveRequest;
import store.onuljang.shared.feign.dto.reseponse.KakaoPayApproveResponse;
import store.onuljang.shared.service.KakaoPayService;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.user.service.UserService;
import store.onuljang.shared.util.DisplayCodeGenerator;
import store.onuljang.shared.util.TimeUtil;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierOrderAppService {

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    UserService userService;
    CourierOrderService courierOrderService;
    CourierPaymentService courierPaymentService;
    CourierPaymentProcessor courierPaymentProcessor;
    CourierProductService courierProductService;
    CourierShippingFeeService courierShippingFeeService;
    KakaoPayService kakaoPayService;

    @Transactional
    public CourierOrderReadyResponse ready(String uid, CourierOrderReadyRequest request) {
        Users user = userService.findByUId(uid);
        PaymentProvider provider;
        try {
            provider = PaymentProvider.valueOf(request.pgProvider().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UserValidateException("존재하지 않는 결제 수단입니다: " + request.pgProvider());
        }

        // 1) 멱등성 키 확인
        CourierOrder existing =
                courierOrderService.findByIdempotencyKey(user, request.idempotencyKey()).orElse(null);
        if (existing != null) {
            if (existing.getStatus() == CourierOrderStatus.PENDING_PAYMENT) {
                return courierPaymentProcessor.preparePayment(existing, user, provider);
            }
            return CourierOrderReadyResponse.builder()
                    .displayCode(existing.getDisplayCode())
                    .redirectUrl("/shop/orders/" + existing.getDisplayCode())
                    .build();
        }

        // 기존 미결제 주문 자동 취소
        cancelPendingPayments(user);

        // 2) 모든 상품 재고 검증 + 수량 합산
        int totalQuantity = 0;
        BigDecimal productAmount = BigDecimal.ZERO;
        List<CourierOrderItemData> itemDataList = new ArrayList<>();

        for (CourierOrderReadyRequest.OrderItemRequest itemReq : request.items()) {
            CourierProduct product = courierProductService.findByIdWithLock(itemReq.courierProductId());
            product.assertPurchasable(itemReq.quantity());

            // 옵션 추가 가격 계산
            BigDecimal optionAdditionalPrice = BigDecimal.ZERO;
            String selectedOptionsJson = null;
            List<Long> selectedOptIds = new ArrayList<>();
            if (itemReq.selectedOptionIds() != null && !itemReq.selectedOptionIds().isEmpty()) {
                List<SelectedOptionSnapshot> snapshots = new ArrayList<>();
                for (CourierProductOptionGroup group : product.getOptionGroups()) {
                    for (CourierProductOption opt : group.getOptions()) {
                        if (itemReq.selectedOptionIds().contains(opt.getId())) {
                            opt.assertPurchasable(itemReq.quantity());
                            selectedOptIds.add(opt.getId());
                            optionAdditionalPrice = optionAdditionalPrice.add(opt.getAdditionalPrice());
                            snapshots.add(new SelectedOptionSnapshot(
                                    group.getName(), opt.getName(), opt.getAdditionalPrice()));
                        }
                    }
                }
                if (!snapshots.isEmpty()) {
                    try {
                        selectedOptionsJson = OBJECT_MAPPER.writeValueAsString(snapshots);
                    } catch (Exception e) {
                        throw new IllegalStateException("옵션 정보 직렬화 실패", e);
                    }
                }
            }

            String selectedOptionIdsStr = selectedOptIds.isEmpty()
                    ? null
                    : selectedOptIds.stream().map(String::valueOf).collect(Collectors.joining(","));

            BigDecimal unitPrice = product.getPrice().add(optionAdditionalPrice);
            BigDecimal itemAmount = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));
            totalQuantity += itemReq.quantity();
            productAmount = productAmount.add(itemAmount);

            itemDataList.add(new CourierOrderItemData(
                    product, itemReq.quantity(), itemAmount, selectedOptionsJson, selectedOptionIdsStr));
        }

        // 3) 배송비 계산 (상품별 템플릿 기반)
        List<ShippingFeeItemInput> feeItems =
                itemDataList.stream()
                        .map(
                                data ->
                                        new ShippingFeeItemInput(
                                                data.product().getId(),
                                                data.quantity(),
                                                data.amount(),
                                                data.product().getShippingFeeTemplate() != null
                                                        ? data.product()
                                                                .getShippingFeeTemplate()
                                                                .getId()
                                                        : null))
                        .toList();
        ShippingFeeResult feeResult =
                courierShippingFeeService.calculateByItems(feeItems, request.postalCode());
        BigDecimal totalAmount = productAmount.add(feeResult.totalShippingFee());

        // 4) 주문 생성
        String displayCode =
                DisplayCodeGenerator.generateUnique(
                        "C", TimeUtil.nowDateTime(), courierOrderService::existsByDisplayCode);

        CourierOrder order =
                CourierOrder.builder()
                        .user(user)
                        .displayCode(displayCode)
                        .status(CourierOrderStatus.PENDING_PAYMENT)
                        .receiverName(request.receiverName())
                        .receiverPhone(request.receiverPhone())
                        .postalCode(request.postalCode())
                        .address1(request.address1())
                        .address2(request.address2())
                        .shippingMemo(request.shippingMemo())
                        .isIsland(feeResult.isIsland())
                        .productAmount(productAmount)
                        .shippingFee(feeResult.shippingFee())
                        .islandSurcharge(feeResult.islandSurcharge())
                        .totalAmount(totalAmount)
                        .idempotencyKey(request.idempotencyKey())
                        .build();

        CourierOrder saved = courierOrderService.save(order);

        // 5) 주문 항목 생성 + 재고 차감
        for (CourierOrderItemData data : itemDataList) {
            data.product.purchase(data.quantity);
            // 옵션 재고 차감
            if (data.selectedOptionIds() != null && !data.selectedOptionIds().isBlank()) {
                Set<Long> optIds = Arrays.stream(data.selectedOptionIds().split(","))
                        .map(String::trim)
                        .map(Long::valueOf)
                        .collect(Collectors.toSet());
                for (CourierProductOptionGroup group : data.product.getOptionGroups()) {
                    for (CourierProductOption opt : group.getOptions()) {
                        if (optIds.contains(opt.getId())) {
                            opt.purchase(data.quantity);
                        }
                    }
                }
            }
            saved.getItems()
                    .add(
                            CourierOrderItem.builder()
                                    .courierOrder(saved)
                                    .courierProduct(data.product)
                                    .productName(data.product.getName())
                                    .productPrice(data.product.getPrice())
                                    .quantity(data.quantity)
                                    .amount(data.amount)
                                    .selectedOptions(data.selectedOptions())
                                    .selectedOptionIds(data.selectedOptionIds())
                                    .build());
        }

        // 6) PG 결제 준비
        return courierPaymentProcessor.preparePayment(saved, user, provider);
    }

    /**
     * 결제 승인 - 트랜잭션 분리 구조 Step 1: 검증 (읽기 tx) -> Step 2: PG 승인 (tx 없음) -> Step 3: DB 반영 (쓰기
     * tx) PG 호출 중 DB 커넥션을 점유하지 않아 커넥션 풀 고갈을 방지한다.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void approve(String uid, String orderIdentifier, String pgToken) {
        // Step 1: 검증 (각 Service 메서드의 읽기 트랜잭션)
        Users user = userService.findByUId(uid);
        String displayCode = DisplayCodeGenerator.resolveCode("C", orderIdentifier);
        CourierOrder order = courierOrderService.findByDisplayCodeAndUser(displayCode, user);
        if (!order.canMarkPaid()) {
            throw new UserValidateException("결제 진행 상태가 아닙니다.");
        }

        // Step 2: 카카오페이 승인 (트랜잭션 밖 - DB 커넥션 미점유, @Retryable로 재시도)
        KakaoPayApproveResponse pgResponse =
                kakaoPayService.approve(
                        new KakaoPayApproveRequest(
                                null,
                                order.getPgTid(),
                                order.getDisplayCode(),
                                user.getUid(),
                                pgToken));

        // Step 3: DB 반영 (Service 쓰기 트랜잭션)
        courierOrderService.completePaid(order.getId(), pgResponse.tid(), pgResponse.aid());
    }

    @Transactional
    public void cancel(String uid, String orderIdentifier) {
        Users user = userService.findByUId(uid);
        String displayCode = DisplayCodeGenerator.resolveCode("C", orderIdentifier);
        CourierOrder order = courierOrderService.findByDisplayCodeAndUser(displayCode, user);
        if (!order.canCancelByUser()) {
            throw new UserValidateException("이미 결제 완료된 주문입니다.");
        }
        order.markCanceled();
        courierPaymentService.markCanceled(order);
        restoreStock(order);
    }

    @Transactional
    public void fail(String uid, String orderIdentifier) {
        Users user = userService.findByUId(uid);
        String displayCode = DisplayCodeGenerator.resolveCode("C", orderIdentifier);
        CourierOrder order = courierOrderService.findByDisplayCodeAndUser(displayCode, user);
        if (!order.canFailByUser()) {
            throw new UserValidateException("이미 결제 완료된 주문입니다.");
        }
        order.markFailed();
        courierPaymentService.markFailed(order);
        restoreStock(order);
    }

    public List<CourierOrderResponse> getOrders(String uid, Long cursor, int size) {
        Users user = userService.findByUId(uid);
        List<CourierOrder> orders = courierOrderService.findByUser(user, cursor, size);
        return orders.stream().map(CourierOrderResponse::from).toList();
    }

    public CourierOrderDetailResponse getOrderDetail(String uid, String displayCode) {
        Users user = userService.findByUId(uid);
        CourierOrder order = courierOrderService.findByDisplayCodeAndUser(displayCode, user);
        return CourierOrderDetailResponse.from(order);
    }

    private void cancelPendingPayments(Users user) {
        List<CourierOrder> pending = courierOrderService.findPendingPaymentsByUser(user);
        for (CourierOrder order : pending) {
            order.markCanceled();
            courierPaymentService.markCanceled(order);
            restoreStock(order);
        }
    }

    private void restoreStock(CourierOrder order) {
        for (CourierOrderItem item : order.getItems()) {
            if (item.getCourierProduct() != null) {
                item.getCourierProduct().restoreStock(item.getQuantity());
            }
            // 옵션 재고 복원
            if (item.getSelectedOptionIds() != null
                    && !item.getSelectedOptionIds().isBlank()
                    && item.getCourierProduct() != null) {
                Set<Long> optIds = Arrays.stream(item.getSelectedOptionIds().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .collect(Collectors.toSet());
                for (CourierProductOptionGroup group : item.getCourierProduct().getOptionGroups()) {
                    for (CourierProductOption opt : group.getOptions()) {
                        if (optIds.contains(opt.getId())) {
                            opt.restoreStock(item.getQuantity());
                        }
                    }
                }
            }
        }
    }

    private record CourierOrderItemData(
            CourierProduct product,
            int quantity,
            BigDecimal amount,
            String selectedOptions,
            String selectedOptionIds) {}
}
