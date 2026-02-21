package store.onuljang.courier.service;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.dto.CourierOrderReadyResponse;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierPayment;
import store.onuljang.shared.config.KakaoPayConfigDto;
import store.onuljang.shared.entity.enums.CourierPaymentStatus;
import store.onuljang.shared.entity.enums.PaymentProvider;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.feign.dto.request.KakaoPayReadyRequest;
import store.onuljang.shared.feign.dto.reseponse.KakaoPayReadyResponse;
import store.onuljang.shared.service.KakaoPayService;
import store.onuljang.shared.service.NaverPayService;
import store.onuljang.shared.service.TossPayService;
import store.onuljang.shared.user.entity.Users;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierPaymentProcessor {

    KakaoPayService kakaoPayService;
    KakaoPayConfigDto kakaoPayConfigDto;
    NaverPayService naverPayService;
    TossPayService tossPayService;
    CourierPaymentService courierPaymentService;

    @Transactional
    public CourierOrderReadyResponse preparePayment(
            CourierOrder order, Users user, PaymentProvider provider) {
        return switch (provider) {
            case KAKAOPAY -> prepareKakaoPay(order, user);
            case NAVERPAY -> prepareNaverPay(order);
            case TOSSPAY -> prepareTossPay(order);
        };
    }

    @Transactional
    public CourierOrderReadyResponse preparePayment(CourierOrder order, Users user) {
        return preparePayment(order, user, PaymentProvider.KAKAOPAY);
    }

    private CourierOrderReadyResponse prepareKakaoPay(CourierOrder order, Users user) {
        if (!kakaoPayConfigDto.isEnabled()) {
            return handleImmediatePayment(order);
        }

        int totalAmount = order.getTotalAmount().intValue();
        KakaoPayConfigDto.KakaoPayRedirectUrls redirectUrls =
                kakaoPayConfigDto.buildCourierRedirectUrls(order.getDisplayCode());

        KakaoPayReadyResponse ready =
                kakaoPayService.ready(
                        new KakaoPayReadyRequest(
                                null,
                                order.getDisplayCode(),
                                user.getUid(),
                                order.getProductSummary(),
                                order.getTotalQuantity(),
                                totalAmount,
                                0,
                                redirectUrls.approvalUrl(),
                                redirectUrls.cancelUrl(),
                                redirectUrls.failUrl()));

        order.setPgTid(ready.tid());
        courierPaymentService.save(
                CourierPayment.builder()
                        .courierOrder(order)
                        .pgProvider(PaymentProvider.KAKAOPAY)
                        .status(CourierPaymentStatus.READY)
                        .amount(BigDecimal.valueOf(totalAmount))
                        .tid(ready.tid())
                        .build());

        return CourierOrderReadyResponse.builder()
                .displayCode(order.getDisplayCode())
                .redirectUrl(ready.nextRedirectPcUrl())
                .mobileRedirectUrl(ready.nextRedirectMobileUrl())
                .build();
    }

    private CourierOrderReadyResponse prepareNaverPay(CourierOrder order) {
        if (!naverPayService.isEnabled()) {
            throw new UserValidateException("네이버페이는 현재 사용할 수 없습니다.");
        }

        int totalAmount = order.getTotalAmount().intValue();
        courierPaymentService.save(
                CourierPayment.builder()
                        .courierOrder(order)
                        .pgProvider(PaymentProvider.NAVERPAY)
                        .status(CourierPaymentStatus.READY)
                        .amount(BigDecimal.valueOf(totalAmount))
                        .build());

        // TODO: NaverPay API 연동 후 redirect URL 반환
        throw new UserValidateException("네이버페이 연동이 준비 중입니다.");
    }

    private CourierOrderReadyResponse prepareTossPay(CourierOrder order) {
        if (!tossPayService.isEnabled()) {
            throw new UserValidateException("토스페이는 현재 사용할 수 없습니다.");
        }

        int totalAmount = order.getTotalAmount().intValue();
        courierPaymentService.save(
                CourierPayment.builder()
                        .courierOrder(order)
                        .pgProvider(PaymentProvider.TOSSPAY)
                        .status(CourierPaymentStatus.READY)
                        .amount(BigDecimal.valueOf(totalAmount))
                        .build());

        // TODO: TossPay - FE SDK handles payment, server only confirms
        throw new UserValidateException("토스페이 연동이 준비 중입니다.");
    }

    private CourierOrderReadyResponse handleImmediatePayment(CourierOrder order) {
        order.markPaid(null);
        return CourierOrderReadyResponse.builder()
                .displayCode(order.getDisplayCode())
                .redirectUrl("/shop/orders/" + order.getDisplayCode())
                .build();
    }
}
