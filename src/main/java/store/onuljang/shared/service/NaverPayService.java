package store.onuljang.shared.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store.onuljang.shared.config.NaverPayConfigDto;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class NaverPayService {

    NaverPayConfigDto naverPayConfigDto;

    public boolean isEnabled() {
        return naverPayConfigDto.isEnabled();
    }

    /**
     * 네이버페이 결제 준비 요청.
     * @return redirect URL for NaverPay payment page
     */
    public String ready(String orderId, String itemName, int totalAmount, String returnUrl) {
        // TODO: 네이버페이 API 연동
        throw new UnsupportedOperationException("네이버페이 연동이 아직 구현되지 않았습니다.");
    }

    /**
     * 네이버페이 결제 승인.
     * @return PG transaction ID (tid)
     */
    public String approve(String paymentId, String orderId) {
        // TODO: 네이버페이 API 연동
        throw new UnsupportedOperationException("네이버페이 연동이 아직 구현되지 않았습니다.");
    }

    /**
     * 네이버페이 결제 취소.
     */
    public void cancel(String paymentId, int cancelAmount, String reason) {
        // TODO: 네이버페이 API 연동
        throw new UnsupportedOperationException("네이버페이 연동이 아직 구현되지 않았습니다.");
    }
}
