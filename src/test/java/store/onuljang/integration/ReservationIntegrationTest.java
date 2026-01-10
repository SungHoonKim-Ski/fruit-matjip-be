package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.controller.response.ReservationListResponse;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.support.IntegrationTestBase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static store.onuljang.util.TimeUtil.nowDate;

/**
 * 예약 API 통합 테스트
 *
 * API Spec: - POST /api/auth/reservations/ - 예약 생성 - PATCH
 * /api/auth/reservations/cancel/{id} - 예약 취소 - PATCH
 * /api/auth/reservations/{id}/quantity?minus={value} - 수량 감소 - PATCH
 * /api/auth/reservations/self-pick/{id} - 셀프 픽업 요청 - GET
 * /api/auth/reservations/?from={date}&to={date} - 예약 목록 조회
 */
class ReservationIntegrationTest extends IntegrationTestBase {

    private Users user;
    private String accessToken;
    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        user = testFixture.createUser("테스트유저");
        accessToken = testFixture.createAccessToken(user);
    }

    @Nested
    @DisplayName("POST /api/auth/reservations/ - 예약 생성")
    class CreateReservation {

        @Test
        @DisplayName("예약 생성 성공")
        void createReservation_Success() throws Exception {
            // given
            Product product = testFixture.createFutureProduct("테스트상품", 10, new BigDecimal("10000"), 1, admin);
            ReservationRequest request = new ReservationRequest(product.getId(), 2, new BigDecimal("20000"));

            // when
            var response = postAction("/api/auth/reservations/", request, accessToken, Long.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body()).isNotNull();
        }

        @Test
        @DisplayName("재고 부족 시 예약 실패")
        void createReservation_InsufficientStock() throws Exception {
            // given
            // 재고 부족 테스트 - 내일 상품으로 시간 무관하게 동작
            Product product = testFixture.createTomorrowProduct("테스트상품", 1, new BigDecimal("10000"), admin);
            ReservationRequest request = new ReservationRequest(product.getId(), 5, // 재고보다 많은 수량
                    new BigDecimal("50000"));

            // when
            var response = postAction("/api/auth/reservations/", request, accessToken, Void.class);

            // then
            assertThat(response.status()).isEqualTo(409);
        }

        @Test
        @DisplayName("비공개 상품 예약 시 실패")
        void createReservation_InvisibleProduct() throws Exception {
            // given
            Product product = testFixture.createInvisibleProduct("비공개상품", 10, new BigDecimal("10000"), nowDate(),
                    admin);
            ReservationRequest request = new ReservationRequest(product.getId(), 1, new BigDecimal("10000"));

            // when
            var response = postAction("/api/auth/reservations/", request, accessToken, Void.class);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }

        @Test
        @DisplayName("닉네임 미변경 사용자 예약 시 실패")
        void createReservation_NicknameNotChanged() throws Exception {
            // given
            Users newUser = testFixture.createNewUser();
            String newUserToken = testFixture.createAccessToken(newUser);
            Product product = testFixture.createFutureProduct("테스트상품", 10, new BigDecimal("10000"), 1, admin);

            ReservationRequest request = new ReservationRequest(product.getId(), 1, new BigDecimal("10000"));

            // when
            var response = postAction("/api/auth/reservations/", request, newUserToken, Void.class);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 상품 예약 시 실패")
        void createReservation_ProductNotFound() throws Exception {
            // given
            ReservationRequest request = new ReservationRequest(99999L, 1, new BigDecimal("10000"));

            // when
            var response = postAction("/api/auth/reservations/", request, accessToken, Void.class);

            // then
            assertThat(response.isNotFound()).isTrue();
        }
    }

    @Nested
    @DisplayName("PATCH /api/auth/reservations/cancel/{id} - 예약 취소")
    class CancelReservation {

        @Test
        @DisplayName("예약 취소 성공")
        void cancelReservation_Success() throws Exception {
            // given
            Product product = testFixture.createFutureProduct("테스트상품", 10, new BigDecimal("10000"), 1, admin);
            Reservation reservation = testFixture.createReservation(user, product, 2);

            // when
            var response = patchAction("/api/auth/reservations/cancel/" + reservation.getId(), accessToken);

            // then
            assertThat(response.isOk()).isTrue();
        }

        @Test
        @DisplayName("다른 사용자의 예약 취소 시 실패")
        void cancelReservation_NotOwner() throws Exception {
            // given
            Users otherUser = testFixture.createUser("다른유저");
            Product product = testFixture.createFutureProduct("테스트상품", 10, new BigDecimal("10000"), 1, admin);
            Reservation reservation = testFixture.createReservation(otherUser, product, 2);

            // when
            var response = patchAction("/api/auth/reservations/cancel/" + reservation.getId(), accessToken);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }

        @Test
        @DisplayName("이미 취소된 예약 취소 시 실패")
        void cancelReservation_AlreadyCanceled() throws Exception {
            // given
            Product product = testFixture.createFutureProduct("테스트상품", 10, new BigDecimal("10000"), 1, admin);
            Reservation reservation = testFixture.createReservationWithStatus(user, product, 2,
                    ReservationStatus.CANCELED);

            // when
            var response = patchAction("/api/auth/reservations/cancel/" + reservation.getId(), accessToken);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }

        @Test
        @DisplayName("수령 완료된 예약 취소 시 실패")
        void cancelReservation_AlreadyPicked() throws Exception {
            // given
            Product product = testFixture.createFutureProduct("테스트상품", 10, new BigDecimal("10000"), 1, admin);
            Reservation reservation = testFixture.createReservationWithStatus(user, product, 2,
                    ReservationStatus.PICKED);

            // when
            var response = patchAction("/api/auth/reservations/cancel/" + reservation.getId(), accessToken);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }
    }

    @Nested
    @DisplayName("PATCH /api/auth/reservations/{id}/quantity - 수량 감소")
    class ReduceQuantity {

        @Test
        @DisplayName("수량 감소 성공")
        void reduceQuantity_Success() throws Exception {
            // given
            Product product = testFixture.createFutureProduct("테스트상품", 10, new BigDecimal("10000"), 1, admin);
            Reservation reservation = testFixture.createReservation(user, product, 5);

            // when
            var response = patchAction("/api/auth/reservations/" + reservation.getId() + "/quantity?minus=2",
                    accessToken);

            // then
            assertThat(response.isOk()).isTrue();
        }

        @Test
        @DisplayName("수량이 1 미만이 되는 경우 실패")
        void reduceQuantity_BelowMinimum() throws Exception {
            // given
            Product product = testFixture.createFutureProduct("테스트상품", 10, new BigDecimal("10000"), 1, admin);
            Reservation reservation = testFixture.createReservation(user, product, 2);

            // when
            var response = patchAction("/api/auth/reservations/" + reservation.getId() + "/quantity?minus=2",
                    accessToken);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }
    }

    @Nested
    @DisplayName("PATCH /api/auth/reservations/self-pick/{id} - 셀프 픽업 요청")
    class RequestSelfPick {

        @Test
        @DisplayName("셀프 픽업 요청 성공")
        void requestSelfPick_Success() throws Exception {
            // given
            Product product = testFixture.createFutureProduct("테스트상품", 10, new BigDecimal("10000"), 1, admin);
            Reservation reservation = testFixture.createReservation(user, product, 2);

            // when
            var response = patchAction("/api/auth/reservations/self-pick/" + reservation.getId(), accessToken);

            // then
            assertThat(response.isOk()).isTrue();
        }

        @Test
        @DisplayName("경고 횟수 초과 사용자 셀프 픽업 요청 시 실패")
        void requestSelfPick_ExceedWarnCount() throws Exception {
            // given
            Users warnedUser = testFixture.createUserWithWarns("경고유저", 2);
            String warnedUserToken = testFixture.createAccessToken(warnedUser);
            Product product = testFixture.createFutureProduct("테스트상품", 10, new BigDecimal("10000"), 1, admin);
            Reservation reservation = testFixture.createReservation(warnedUser, product, 2);

            // when
            var response = patchAction("/api/auth/reservations/self-pick/" + reservation.getId(), warnedUserToken);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }

        @Test
        @DisplayName("셀프 픽업 불가 상품 요청 시 실패")
        void requestSelfPick_ProductNotAllowed() throws Exception {
            // given
            Product product = testFixture.createNoSelfPickProduct("셀프픽업불가상품", 10, new BigDecimal("10000"),
                    nowDate(), admin);
            Reservation reservation = testFixture.createReservation(user, product, 2);

            // when
            var response = patchAction("/api/auth/reservations/self-pick/" + reservation.getId(), accessToken);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/auth/reservations/ - 예약 목록 조회")
    class GetReservations {

        @Test
        @DisplayName("예약 목록 조회 성공")
        void getReservations_Success() throws Exception {
            // given
            Product product1 = testFixture.createTodayProduct("상품1", 10, new BigDecimal("10000"), admin);
            Product product2 = testFixture.createTodayProduct("상품2", 10, new BigDecimal("20000"), admin);

            testFixture.createReservation(user, product1, 2);
            testFixture.createReservation(user, product2, 1);

            LocalDate today = nowDate();
            String fromDate = today.format(DateTimeFormatter.ISO_DATE);
            String toDate = today.format(DateTimeFormatter.ISO_DATE);

            // when
            var response = getAction("/api/auth/reservations/?from=" + fromDate + "&to=" + toDate, accessToken,
                    ReservationListResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).hasSize(2);
        }

        @Test
        @DisplayName("다른 사용자의 예약은 조회되지 않음")
        void getReservations_OnlyOwnReservations() throws Exception {
            // given
            Users otherUser = testFixture.createUser("다른유저");
            Product product = testFixture.createTomorrowProduct("상품", 10, new BigDecimal("10000"), admin);

            testFixture.createReservation(user, product, 2);
            testFixture.createReservation(otherUser, product, 1);

            LocalDate tomorrow = nowDate().plusDays(1);
            String fromDate = tomorrow.format(DateTimeFormatter.ISO_DATE);
            String toDate = tomorrow.format(DateTimeFormatter.ISO_DATE);

            // when
            var response = getAction("/api/auth/reservations/?from=" + fromDate + "&to=" + toDate, accessToken,
                    ReservationListResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).hasSize(1);
        }

        @Test
        @DisplayName("예약이 없는 경우 빈 배열 반환")
        void getReservations_Empty() throws Exception {
            // given
            LocalDate today = nowDate();
            String fromDate = today.format(DateTimeFormatter.ISO_DATE);
            String toDate = today.format(DateTimeFormatter.ISO_DATE);

            // when
            var response = getAction("/api/auth/reservations/?from=" + fromDate + "&to=" + toDate, accessToken,
                    ReservationListResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body().response()).isEmpty();
        }
    }
}
