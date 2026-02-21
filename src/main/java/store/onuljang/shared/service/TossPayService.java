package store.onuljang.shared.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store.onuljang.shared.config.TossPayConfigDto;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TossPayService {

    TossPayConfigDto tossPayConfigDto;

    public boolean isEnabled() {
        return tossPayConfigDto.isEnabled();
    }

    /**
     * 토스페이 결제 확인(confirm).
     * FE에서 TossPayments SDK로 결제 후 paymentKey, orderId, amount를 받아서 서버에서 confirm.
     * @return PG transaction ID
     */
    public String confirm(String paymentKey, String orderId, int amount) {
        // TODO: 토스페이 API 연동
        throw new UnsupportedOperationException("토스페이 연동이 아직 구현되지 않았습니다.");
    }

    /**
     * 토스페이 결제 취소.
     */
    public void cancel(String paymentKey, int cancelAmount, String reason) {
        // TODO: 토스페이 API 연동
        throw new UnsupportedOperationException("토스페이 연동이 아직 구현되지 않았습니다.");
    }
}
