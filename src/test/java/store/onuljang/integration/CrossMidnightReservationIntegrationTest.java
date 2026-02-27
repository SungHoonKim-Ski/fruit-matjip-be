package store.onuljang.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store.onuljang.shop.admin.dto.AdminStoreConfigRequest;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.product.entity.Product;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shop.reservation.service.StoreConfigService;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.util.TimeUtil;
import store.onuljang.support.IntegrationTestBase;

import java.math.BigDecimal;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

class CrossMidnightReservationIntegrationTest extends IntegrationTestBase {

    private record ErrorResponse(String state, String message) {
    }

    @Autowired
    private StoreConfigService storeConfigService;

    private Users user;
    private String accessToken;
    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        user = testFixture.createUser("크로스미드나잇");
        accessToken = testFixture.createAccessToken(user);

        // cross-midnight 설정: 예약 마감 24:30, 취소 마감 24:00, 수령 마감 25:00
        storeConfigService.update(new AdminStoreConfigRequest(24, 30, 24, 0, 25, 0));
    }

    @AfterEach
    void tearDown() {
        TimeUtil.resetClock();
    }

    @Test
    @DisplayName("자정 이후 취소 마감(24:00=익일 00:00) 전에 취소 가능")
    void cancel_CrossMidnight_BeforeCancellationDeadline_Success() throws Exception {
        // given - 2026-01-15의 상품, 시각을 23:30으로 고정 (취소 마감 24:00 전)
        LocalDate sellDate = LocalDate.of(2026, 1, 15);
        ZonedDateTime fixed = ZonedDateTime.of(sellDate, LocalTime.of(23, 30), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(fixed.toInstant(), TimeUtil.KST));

        Product product = testFixture.createProduct("심야상품", 10, new BigDecimal("10000"), sellDate, admin);
        Reservation reservation = testFixture.createReservation(user, product, 2);

        // when
        var response = patchAction(
            "/api/store/auth/reservations/cancel/" + reservation.getDisplayCode(),
            null, accessToken, Void.class);

        // then
        assertThat(response.isOk()).isTrue();
    }

    @Test
    @DisplayName("자정 이후 취소 마감(24:00=익일 00:00) 이후에 취소 불가")
    void cancel_CrossMidnight_AfterCancellationDeadline_Fail() throws Exception {
        // given - 2026-01-15의 상품, 시각을 익일 00:30으로 고정 (취소 마감 24:00 이후)
        LocalDate sellDate = LocalDate.of(2026, 1, 15);
        ZonedDateTime fixed = ZonedDateTime.of(
            sellDate.plusDays(1), LocalTime.of(0, 30), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(fixed.toInstant(), TimeUtil.KST));

        Product product = testFixture.createProduct("심야상품2", 10, new BigDecimal("10000"), sellDate, admin);
        Reservation reservation = testFixture.createReservation(user, product, 2);

        // when
        var response = patchAction(
            "/api/store/auth/reservations/cancel/" + reservation.getDisplayCode(),
            null, accessToken, ErrorResponse.class);

        // then
        assertThat(response.isBadRequest()).isTrue();
        assertThat(response.body().message()).isEqualTo("취소 가능 시각이 지났습니다.");
    }

    @Test
    @DisplayName("자정 이후 수령 마감(25:00=익일 01:00) 이후 과거 취급")
    void cancel_CrossMidnight_AfterPickupDeadline_PastDate() throws Exception {
        // given - 2026-01-15의 상품, 시각을 익일 01:30으로 고정 (수령 마감 25:00 이후)
        LocalDate sellDate = LocalDate.of(2026, 1, 15);
        ZonedDateTime fixed = ZonedDateTime.of(
            sellDate.plusDays(1), LocalTime.of(1, 30), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(fixed.toInstant(), TimeUtil.KST));

        Product product = testFixture.createProduct("심야상품3", 10, new BigDecimal("10000"), sellDate, admin);
        Reservation reservation = testFixture.createReservation(user, product, 2);

        // when
        var response = patchAction(
            "/api/store/auth/reservations/cancel/" + reservation.getDisplayCode(),
            null, accessToken, ErrorResponse.class);

        // then
        assertThat(response.isBadRequest()).isTrue();
        assertThat(response.body().message()).isEqualTo("과거 예약은 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("cross-midnight 예약 마감(24:30) 전 예약 가능")
    void reserve_CrossMidnight_BeforeReservationDeadline_Success() throws Exception {
        // given - 시각 24:00 (자정) = 취소 마감이지만 예약 마감(24:30) 전
        LocalDate sellDate = LocalDate.of(2026, 1, 15);
        ZonedDateTime fixed = ZonedDateTime.of(
            sellDate.plusDays(1), LocalTime.of(0, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(fixed.toInstant(), TimeUtil.KST));

        Product product = testFixture.createProduct("심야예약상품", 10, new BigDecimal("10000"), sellDate, admin);

        // when
        var response = postAction(
            "/api/store/auth/reservations/",
            new store.onuljang.shop.reservation.dto.ReservationRequest(product.getId(), 1),
            accessToken, Void.class);

        // then
        assertThat(response.isOk()).isTrue();
    }

    @Test
    @DisplayName("cross-midnight 예약 마감(24:30) 이후 예약 불가")
    void reserve_CrossMidnight_AfterReservationDeadline_Fail() throws Exception {
        // given - 시각 00:31 (예약 마감 24:30 이후)
        LocalDate sellDate = LocalDate.of(2026, 1, 15);
        ZonedDateTime fixed = ZonedDateTime.of(
            sellDate.plusDays(1), LocalTime.of(0, 31), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(fixed.toInstant(), TimeUtil.KST));

        Product product = testFixture.createProduct("심야예약상품2", 10, new BigDecimal("10000"), sellDate, admin);

        // when
        var response = postAction(
            "/api/store/auth/reservations/",
            new store.onuljang.shop.reservation.dto.ReservationRequest(product.getId(), 1),
            accessToken, ErrorResponse.class);

        // then
        assertThat(response.isBadRequest()).isTrue();
        assertThat(response.body().message()).isEqualTo("예약 가능한 시간이 지났습니다.");
    }
}
