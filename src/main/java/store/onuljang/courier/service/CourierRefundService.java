package store.onuljang.courier.service;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierPayment;
import store.onuljang.shared.feign.dto.request.KakaoPayCancelRequest;
import store.onuljang.shared.service.KakaoPayService;
import store.onuljang.shared.service.NaverPayService;
import store.onuljang.shared.service.TossPayService;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierRefundService {

    KakaoPayService kakaoPayService;
    NaverPayService naverPayService;
    TossPayService tossPayService;
    CourierPaymentService courierPaymentService;

    public void refund(CourierOrder order, BigDecimal refundAmount) {
        CourierPayment payment = courierPaymentService.findByCourierOrder(order).orElse(null);
        if (payment == null || order.getPgTid() == null) {
            log.warn("결제 정보 없음, 환불 스킵 (orderId={})", order.getId());
            return;
        }

        try {
            switch (payment.getPgProvider()) {
                case KAKAOPAY -> kakaoPayService.cancel(
                        new KakaoPayCancelRequest(
                                null, order.getPgTid(), refundAmount.intValue(), 0));
                case NAVERPAY -> naverPayService.cancel(
                        order.getPgTid(), refundAmount.intValue(), "클레임 환불");
                case TOSSPAY -> tossPayService.cancel(
                        order.getPgTid(), refundAmount.intValue(), "클레임 환불");
            }
        } catch (Exception e) {
            log.warn(
                    "PG 환불 실패 (orderId={}, provider={}, amount={}): {}",
                    order.getId(),
                    payment.getPgProvider(),
                    refundAmount,
                    e.getMessage());
            throw new RuntimeException("PG 환불 처리 중 오류가 발생했습니다.", e);
        }

        if (refundAmount.compareTo(payment.getAmount()) >= 0) {
            payment.markCanceled();
        } else {
            payment.markPartialCanceled(refundAmount);
        }
    }
}
