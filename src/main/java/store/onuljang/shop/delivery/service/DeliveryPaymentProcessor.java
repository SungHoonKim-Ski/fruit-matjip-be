package store.onuljang.shop.delivery.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shared.config.KakaoPayConfigDto;
import store.onuljang.shop.delivery.dto.DeliveryReadyResponse;
import store.onuljang.shop.delivery.event.DeliveryPaidEvent;
import store.onuljang.shared.feign.dto.request.KakaoPayReadyRequest;
import store.onuljang.shared.feign.dto.reseponse.KakaoPayReadyResponse;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shop.delivery.entity.DeliveryPayment;
import store.onuljang.shared.service.KakaoPayService;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shop.delivery.entity.DeliveryPaymentStatus;
import store.onuljang.shared.entity.enums.PaymentProvider;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryPaymentProcessor {
    KakaoPayService kakaoPayService;
    KakaoPayConfigDto kakaoPayConfigDto;
    DeliveryPaymentService deliveryPaymentService;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    public DeliveryReadyResponse preparePayment(DeliveryOrder order, Users user, List<Reservation> reservations,
            BigDecimal totalProductAmount, BigDecimal deliveryFee) {
        // 결제 분기: PG 사용 여부에 따라 즉시 처리 또는 카카오페이 준비
        if (!kakaoPayConfigDto.isEnabled()) {
            return handleImmediatePayment(order);
        }

        // 결제 준비에 필요한 데이터 구성
        int totalAmount = toTotalAmount(totalProductAmount, deliveryFee);
        KakaoPayConfigDto.KakaoPayRedirectUrls redirectUrls = kakaoPayConfigDto.buildRedirectUrls(order.getDisplayCode());

        // 카카오페이 ready 호출 및 결제 정보 저장
        KakaoPayReadyResponse ready = requestReady(order, user, reservations, totalAmount, redirectUrls);
        savePaymentReady(order, totalAmount, ready);

        return DeliveryReadyResponse.builder()
            .orderCode(order.getDisplayCode())
            .redirectUrl(ready.nextRedirectPcUrl())
            .mobileRedirectUrl(ready.nextRedirectMobileUrl())
            .build();
    }

    // 카카오페이 비활성 시 즉시 결제 완료 처리
    private DeliveryReadyResponse handleImmediatePayment(DeliveryOrder order) {
        order.markPaid();
        eventPublisher.publishEvent(new DeliveryPaidEvent(order.getId()));
        return DeliveryReadyResponse.builder()
            .orderCode(order.getDisplayCode())
            .redirectUrl("/me/orders?tab=delivery")
            .build();
    }

    // 총 결제 금액 계산(상품 + 배달)
    private int toTotalAmount(BigDecimal totalProductAmount, BigDecimal deliveryFee) {
        return totalProductAmount.add(deliveryFee).intValue();
    }

    // 카카오페이 ready 요청 수행
    private KakaoPayReadyResponse requestReady(DeliveryOrder order, Users user,
            List<Reservation> reservations, int totalAmount, KakaoPayConfigDto.KakaoPayRedirectUrls redirectUrls) {
        return kakaoPayService.ready(
            new KakaoPayReadyRequest(
                null,
                order.getDisplayCode(),
                user.getUid(),
                Reservation.buildSummary(reservations),
                reservations.stream().mapToInt(Reservation::getQuantity).sum(),
                totalAmount,
                0,
                redirectUrls.approvalUrl(),
                redirectUrls.cancelUrl(),
                redirectUrls.failUrl()
            )
        );
    }

    // 결제 준비 상태 저장
    private void savePaymentReady(DeliveryOrder order, int totalAmount, KakaoPayReadyResponse ready) {
        order.setKakaoTid(ready.tid());
        deliveryPaymentService.save(DeliveryPayment.builder()
            .deliveryOrder(order)
            .pgProvider(PaymentProvider.KAKAOPAY)
            .status(DeliveryPaymentStatus.READY)
            .amount(BigDecimal.valueOf(totalAmount))
            .tid(ready.tid())
            .build());
    }
}
