package store.onuljang.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.TestS3Config;
import store.onuljang.config.TestSqsConfig;
import store.onuljang.shop.admin.dto.AdminStoreConfigRequest;
import store.onuljang.shop.reservation.config.StoreConfigSnapshot;
import store.onuljang.shop.reservation.service.StoreConfigService;
import store.onuljang.shared.exception.AdminValidateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestS3Config.class, TestSqsConfig.class})
@Transactional
class StoreConfigServiceTest {

    @Autowired
    private StoreConfigService storeConfigService;

    @Test
    @DisplayName("getConfig - DB 없을 때 기본값 반환")
    void getConfig_NoDbRow_ReturnsDefaults() {
        // when
        StoreConfigSnapshot config = storeConfigService.getConfig();

        // then
        assertThat(config.reservationDeadlineHour()).isEqualTo(19);
        assertThat(config.reservationDeadlineMinute()).isEqualTo(30);
        assertThat(config.cancellationDeadlineHour()).isEqualTo(19);
        assertThat(config.cancellationDeadlineMinute()).isEqualTo(0);
        assertThat(config.pickupDeadlineHour()).isEqualTo(20);
        assertThat(config.pickupDeadlineMinute()).isEqualTo(0);
    }

    @Test
    @DisplayName("update - 정상 업데이트 후 반환값 확인")
    void update_ValidRequest_ReturnsUpdated() {
        // given
        AdminStoreConfigRequest request = new AdminStoreConfigRequest(20, 0, 19, 30, 21, 0);

        // when
        StoreConfigSnapshot result = storeConfigService.update(request);

        // then
        assertThat(result.reservationDeadlineHour()).isEqualTo(20);
        assertThat(result.reservationDeadlineMinute()).isEqualTo(0);
        assertThat(result.cancellationDeadlineHour()).isEqualTo(19);
        assertThat(result.cancellationDeadlineMinute()).isEqualTo(30);
        assertThat(result.pickupDeadlineHour()).isEqualTo(21);
    }

    @Test
    @DisplayName("update - cross-midnight 시간(25시) 저장 성공")
    void update_CrossMidnightHour_Success() {
        // given
        AdminStoreConfigRequest request = new AdminStoreConfigRequest(24, 30, 24, 0, 25, 0);

        // when
        StoreConfigSnapshot result = storeConfigService.update(request);

        // then
        assertThat(result.pickupDeadlineHour()).isEqualTo(25);
    }

    @Test
    @DisplayName("update - 시간 범위 초과(28시) 시 예외")
    void update_HourOutOfRange_ThrowsException() {
        // given
        AdminStoreConfigRequest request = new AdminStoreConfigRequest(28, 0, 19, 0, 29, 0);

        // when & then
        assertThatThrownBy(() -> storeConfigService.update(request))
            .isInstanceOf(AdminValidateException.class);
    }

    @Test
    @DisplayName("update - 논리 순서 역전(예약마감 > 수령마감) 시 예외")
    void update_InvalidOrder_ThrowsException() {
        // given - reservation(22:00) > pickup(21:00)
        AdminStoreConfigRequest request = new AdminStoreConfigRequest(22, 0, 19, 0, 21, 0);

        // when & then
        assertThatThrownBy(() -> storeConfigService.update(request))
            .isInstanceOf(AdminValidateException.class);
    }

    @Test
    @DisplayName("update - 취소마감 > 예약마감 시 예외")
    void update_CancellationAfterReservation_ThrowsException() {
        // given - cancellation(20:00) > reservation(19:30)
        AdminStoreConfigRequest request = new AdminStoreConfigRequest(19, 30, 20, 0, 21, 0);

        // when & then
        assertThatThrownBy(() -> storeConfigService.update(request))
            .isInstanceOf(AdminValidateException.class);
    }

    @Test
    @DisplayName("update 두 번 호출 - 기존 row 업데이트 확인 (upsert)")
    void update_Twice_UpdatesExistingRow() {
        // given
        AdminStoreConfigRequest first = new AdminStoreConfigRequest(20, 0, 19, 0, 21, 0);
        AdminStoreConfigRequest second = new AdminStoreConfigRequest(21, 0, 20, 0, 22, 0);

        // when
        storeConfigService.update(first);
        StoreConfigSnapshot result = storeConfigService.update(second);

        // then
        assertThat(result.reservationDeadlineHour()).isEqualTo(21);
        assertThat(result.cancellationDeadlineHour()).isEqualTo(20);
        assertThat(result.pickupDeadlineHour()).isEqualTo(22);
    }
}
