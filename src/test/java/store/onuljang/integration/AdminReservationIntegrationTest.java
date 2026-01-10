package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store.onuljang.controller.request.AdminUpdateReservationsRequest;
import store.onuljang.controller.response.AdminReservationListResponse;
import store.onuljang.controller.response.AdminReservationsTodayResponse;
import store.onuljang.repository.ReservationRepository;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.support.IntegrationTestBase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 관리자 예약 관리 API 통합 테스트
 *
 * API Spec: - GET /api/admin/reservations?date={date} - 날짜별 예약 조회 - PATCH
 * /api/admin/reservations/{id}/{status} - 예약 상태 변경 - PATCH
 * /api/admin/reservations/{id}/no-show - 노쇼 처리 - PATCH
 * /api/admin/reservations/status - 일괄 상태 변경 - GET
 * /api/admin/reservations/sales/today - 오늘 판매 현황
 */
class AdminReservationIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Admin admin;
    private Users user1;
    private Users user2;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        user1 = testFixture.createUser("유저1");
        user2 = testFixture.createUser("유저2");
        setAdminAuthentication(admin);
    }

    @Nested
    @DisplayName("GET /api/admin/reservations - 날짜별 예약 조회")
    class GetReservationsByDate {

        @Test
        @DisplayName("날짜별 예약 조회 성공")
        void getReservationsByDate_Success() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("상품", 10, new BigDecimal("10000"), admin);
            testFixture.createReservation(user1, product, 2);
            testFixture.createReservation(user2, product, 3);

            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

            // when
            var response = getAction("/api/admin/reservations?date=" + today, AdminReservationListResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).hasSize(2);
        }

        @Test
        @DisplayName("예약이 없는 날짜 조회 시 빈 배열 반환")
        void getReservationsByDate_Empty() throws Exception {
            // given
            String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);

            // when
            var response = getAction("/api/admin/reservations?date=" + tomorrow, AdminReservationListResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).isEmpty();
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/reservations/{id}/{status} - 예약 상태 변경")
    class UpdateReservationStatus {

        @Test
        @DisplayName("예약 상태를 PICKED로 변경 성공")
        void updateReservationStatus_ToPicked() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("상품", 10, new BigDecimal("10000"), admin);
            Reservation reservation = testFixture.createReservation(user1, product, 2);

            // when
            var response = patchAction("/api/admin/reservations/" + reservation.getId() + "/PICKED");

            // then
            assertThat(response.isOk()).isTrue();

            // 상태 변경 확인
            Reservation updatedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
            assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.PICKED);
        }

        @Test
        @DisplayName("예약 상태를 CANCELED로 변경 성공")
        void updateReservationStatus_ToCanceled() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("상품", 10, new BigDecimal("10000"), admin);
            Reservation reservation = testFixture.createReservation(user1, product, 2);

            // when
            var response = patchAction("/api/admin/reservations/" + reservation.getId() + "/CANCELED");

            // then
            assertThat(response.isOk()).isTrue();

            // 상태 변경 확인
            Reservation updatedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
            assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/reservations/{id}/no-show - 노쇼 처리")
    class HandleNoShow {

        @Test
        @DisplayName("노쇼 처리 성공")
        void handleNoShow_Success() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("상품", 10, new BigDecimal("10000"), admin);
            Reservation reservation = testFixture.createReservation(user1, product, 2);

            // when
            var response = patchAction("/api/admin/reservations/" + reservation.getId() + "/no-show");

            // then
            assertThat(response.isOk()).isTrue();

            // 노쇼 상태 확인
            Reservation updatedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
            assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/reservations/status - 일괄 상태 변경")
    class BulkUpdateStatus {

        @Test
        @DisplayName("여러 예약 일괄 상태 변경 성공")
        void bulkUpdateStatus_Success() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("상품", 10, new BigDecimal("10000"), admin);
            Reservation reservation1 = testFixture.createReservation(user1, product, 2);
            Reservation reservation2 = testFixture.createReservation(user1, product, 3);

            AdminUpdateReservationsRequest request = new AdminUpdateReservationsRequest(
                    Set.of(reservation1.getId(), reservation2.getId()), ReservationStatus.PICKED);

            // when
            var response = patchAction("/api/admin/reservations/status", request, Long.class);

            // then
            assertThat(response.status()).withFailMessage("BulkUpdate: Expected 200 but got " + response.status())
                    .isEqualTo(200);

            // 일괄 변경 확인
            entityManager.flush();
            entityManager.clear();
            Reservation updated1 = reservationRepository.findById(reservation1.getId()).orElseThrow();
            Reservation updated2 = reservationRepository.findById(reservation2.getId()).orElseThrow();
            assertThat(updated1.getStatus()).isEqualTo(ReservationStatus.PICKED);
            assertThat(updated2.getStatus()).isEqualTo(ReservationStatus.PICKED);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/reservations/sales/today - 오늘 판매 현황")
    class GetTodaySales {

        @Test
        @DisplayName("오늘 판매 현황 조회 성공")
        void getTodaySales_Success() throws Exception {
            // given
            Product product = testFixture.createTodayProduct("상품", 10, new BigDecimal("10000"), admin);
            testFixture.createReservationWithStatus(user1, product, 2, ReservationStatus.PICKED);
            testFixture.createReservationWithStatus(user2, product, 3, ReservationStatus.PICKED);

            // when
            var response = getAction("/api/admin/reservations/sales/today", AdminReservationsTodayResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).isNotEmpty();
        }
    }
}
