package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store.onuljang.appservice.AdminReservationAppService;
import store.onuljang.appservice.AdminUserAppService;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.repository.ReservationRepository;
import store.onuljang.repository.UserRepository;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.support.IntegrationTestBase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 이용제한 기능 통합 테스트
 *
 * 요구사항:
 * - 노쇼 배치 시 월별 노쇼 누적(warnCount)에 따라 이용제한 부여
 *   - 1회: 경고만 (추가 조치 없음)
 *   - 2회: 2일 이용제한
 *   - 3회 이상: 5일 이용제한
 * - 이용제한 기간 내 미래 PENDING 예약 자동 취소 + 재고 복원
 * - 이용제한 사용자 예약/배달 주문 거부
 * - 관리자 이용제한 해제
 */
class UserRestrictionIntegrationTest extends IntegrationTestBase {

    @Autowired
    private AdminReservationAppService adminReservationAppService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        setAdminAuthentication(admin);
    }

    @Nested
    @DisplayName("노쇼 배치 - 이용제한 부여")
    class NoShowBatchRestriction {

        @Test
        @DisplayName("warnCount 1회 시 경고만 부여되고 이용제한 없음")
        void noShowBatch_warnCount1_noRestriction() {
            // Arrange
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            Users user = testFixture.createUser("경고1회유저");
            Product product = testFixture.createProduct("오늘상품", 10, new BigDecimal("10000"), today, admin);
            testFixture.createReservation(user, product, 2);

            // Act
            adminReservationAppService.processNoShowBatch(today, LocalDateTime.now());
            entityManager.flush();
            entityManager.clear();

            // Assert
            Users updatedUser = userRepository.findByUid(user.getUid()).orElseThrow();
            assertThat(updatedUser.getMonthlyWarnCount()).isEqualTo(1);
            assertThat(updatedUser.getRestrictedUntil()).isNull();
        }

        @Test
        @DisplayName("warnCount 2회 시 2일 이용제한 부여")
        void noShowBatch_warnCount2_restricted2Days() {
            // Arrange
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            Users user = testFixture.createUserWithWarns("경고2회유저", 1);
            Product product = testFixture.createProduct("오늘상품", 10, new BigDecimal("10000"), today, admin);
            testFixture.createReservation(user, product, 2);

            // Act
            adminReservationAppService.processNoShowBatch(today, LocalDateTime.now());
            entityManager.flush();
            entityManager.clear();

            // Assert
            Users updatedUser = userRepository.findByUid(user.getUid()).orElseThrow();
            assertThat(updatedUser.getMonthlyWarnCount()).isEqualTo(2);
            assertThat(updatedUser.getRestrictedUntil()).isEqualTo(today.plusDays(2));
        }

        @Test
        @DisplayName("warnCount 3회 이상 시 5일 이용제한 부여")
        void noShowBatch_warnCount3_restricted5Days() {
            // Arrange
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            Users user = testFixture.createUserWithWarns("경고3회유저", 2);
            Product product = testFixture.createProduct("오늘상품", 10, new BigDecimal("10000"), today, admin);
            testFixture.createReservation(user, product, 2);

            // Act
            adminReservationAppService.processNoShowBatch(today, LocalDateTime.now());
            entityManager.flush();
            entityManager.clear();

            // Assert
            Users updatedUser = userRepository.findByUid(user.getUid()).orElseThrow();
            assertThat(updatedUser.getMonthlyWarnCount()).isEqualTo(3);
            assertThat(updatedUser.getRestrictedUntil()).isEqualTo(today.plusDays(5));
        }

        @Test
        @DisplayName("이용제한 시 미래 PENDING 예약 자동 취소 및 재고 복원")
        void noShowBatch_restriction_cancelsFutureReservationsAndRestoresStock() {
            // Arrange
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            Users user = testFixture.createUserWithWarns("미래예약취소유저", 1);

            Product todayProduct = testFixture.createProduct("오늘상품", 10, new BigDecimal("10000"), today, admin);
            testFixture.createReservation(user, todayProduct, 2);

            // 내일 상품 (제한 범위 내) - PENDING 예약
            Product tomorrowProduct = testFixture.createProduct("내일상품", 5, new BigDecimal("15000"),
                    today.plusDays(1), admin);
            Reservation futureReservation = testFixture.createReservation(user, tomorrowProduct, 3);

            // 모레 상품 (제한 범위 내) - PENDING 예약
            Product dayAfterProduct = testFixture.createProduct("모레상품", 8, new BigDecimal("20000"),
                    today.plusDays(2), admin);
            Reservation futureReservation2 = testFixture.createReservation(user, dayAfterProduct, 2);

            int tomorrowStockBefore = tomorrowProduct.getStock();
            int dayAfterStockBefore = dayAfterProduct.getStock();

            // Act - warnCount가 2가 되어 2일 제한 부여
            adminReservationAppService.processNoShowBatch(today, LocalDateTime.now());
            entityManager.flush();
            entityManager.clear();

            // Assert - 미래 예약이 CANCELED 상태로 변경
            Reservation updatedFuture1 = reservationRepository.findById(futureReservation.getId()).orElseThrow();
            Reservation updatedFuture2 = reservationRepository.findById(futureReservation2.getId()).orElseThrow();
            assertThat(updatedFuture1.getStatus()).isEqualTo(ReservationStatus.CANCELED);
            assertThat(updatedFuture2.getStatus()).isEqualTo(ReservationStatus.CANCELED);

            // Assert - 재고 복원
            Product updatedTomorrow = entityManager.find(Product.class, tomorrowProduct.getId());
            Product updatedDayAfter = entityManager.find(Product.class, dayAfterProduct.getId());
            assertThat(updatedTomorrow.getStock()).isEqualTo(tomorrowStockBefore + 3);
            assertThat(updatedDayAfter.getStock()).isEqualTo(dayAfterStockBefore + 2);
        }
    }

    @Nested
    @DisplayName("예약 생성 시 이용제한 검증")
    class ReservationRestrictionValidation {

        @Test
        @DisplayName("이용제한 사용자 예약 생성 시 거부")
        void createReservation_restrictedUser_rejected() throws Exception {
            // Arrange
            Users user = testFixture.createUser("제한유저");
            user.restrict(LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(3));
            userRepository.save(user);

            String accessToken = testFixture.createAccessToken(user);
            Product product = testFixture.createFutureProduct("테스트상품", 10, new BigDecimal("10000"), 1, admin);
            ReservationRequest request = new ReservationRequest(product.getId(), 1);

            // Act
            var response = postAction("/api/auth/reservations/", request, accessToken, Void.class);

            // Assert
            assertThat(response.isBadRequest()).isTrue();
        }
    }

    @Nested
    @DisplayName("배달 주문 생성 시 이용제한 검증")
    class DeliveryRestrictionValidation {

        @Test
        @DisplayName("이용제한 사용자 배달 주문 시 거부")
        void createDeliveryOrder_restrictedUser_rejected() throws Exception {
            // Arrange
            Users user = testFixture.createUser("배달제한유저");
            user.restrict(LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(3));
            userRepository.save(user);

            String accessToken = testFixture.createAccessToken(user);
            Product product = testFixture.createTodayProduct("배달상품", 10, new BigDecimal("15000"), admin);
            Reservation reservation = testFixture.createReservation(user, product, 2);

            // 배달 주문 요청
            var request = new store.onuljang.controller.request.DeliveryReadyRequest(
                    java.util.List.of(reservation.getDisplayCode()),
                    12,
                    0,
                    "01012345678",
                    "12345",
                    "서울 강서구 테스트로",
                    "101호",
                    37.556504,
                    126.8372613,
                    null,
                    null,
                    java.util.UUID.randomUUID().toString()
            );

            // Act
            var response = postAction("/api/auth/deliveries/ready", request, accessToken, Void.class);

            // Assert
            assertThat(response.isBadRequest()).isTrue();
        }
    }

    @Nested
    @DisplayName("관리자 이용제한 해제")
    class AdminLiftRestriction {

        @Test
        @DisplayName("관리자가 이용제한 해제 성공")
        void liftRestriction_success() throws Exception {
            // Arrange
            Users user = testFixture.createUser("해제대상유저");
            user.restrict(LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(5));
            userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Act
            var response = patchAction("/api/admin/users/" + user.getUid() + "/lift-restriction");

            // Assert
            assertThat(response.isOk()).isTrue();

            Users updatedUser = userRepository.findByUid(user.getUid()).orElseThrow();
            assertThat(updatedUser.getRestrictedUntil()).isNull();
        }
    }

    @Nested
    @DisplayName("관리자 수동 경고 - 이용제한 부여")
    class AdminWarnRestriction {

        @Autowired
        private AdminUserAppService adminUserAppService;

        @Test
        @DisplayName("관리자 경고로 warnCount 2회 도달 시 2일 이용제한 부여")
        void adminWarn_warnCount2_restricted2Days() {
            // Arrange
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            Users user = testFixture.createUserWithWarns("관리자경고유저", 1);
            entityManager.flush();
            entityManager.clear();

            // Act
            adminUserAppService.warn(java.util.UUID.fromString(user.getUid()));
            entityManager.flush();
            entityManager.clear();

            // Assert
            Users updatedUser = userRepository.findByUid(user.getUid()).orElseThrow();
            assertThat(updatedUser.getMonthlyWarnCount()).isEqualTo(2);
            assertThat(updatedUser.getRestrictedUntil()).isEqualTo(today.plusDays(2));
        }

        @Test
        @DisplayName("관리자 경고로 warnCount 3회 도달 시 5일 이용제한 부여 + 미래 예약 취소")
        void adminWarn_warnCount3_restricted5DaysAndCancelsFutureReservations() {
            // Arrange
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            Users user = testFixture.createUserWithWarns("관리자경고3회", 2);

            Product futureProduct = testFixture.createProduct("미래상품", 10, new BigDecimal("15000"),
                    today.plusDays(1), admin);
            Reservation futureReservation = testFixture.createReservation(user, futureProduct, 3);

            int stockBefore = futureProduct.getStock();
            entityManager.flush();
            entityManager.clear();

            // Act
            adminUserAppService.warn(java.util.UUID.fromString(user.getUid()));
            entityManager.flush();
            entityManager.clear();

            // Assert
            Users updatedUser = userRepository.findByUid(user.getUid()).orElseThrow();
            assertThat(updatedUser.getMonthlyWarnCount()).isEqualTo(3);
            assertThat(updatedUser.getRestrictedUntil()).isEqualTo(today.plusDays(5));

            // 미래 예약 취소 확인
            Reservation updatedReservation = reservationRepository.findById(futureReservation.getId()).orElseThrow();
            assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);

            // 재고 복원 확인
            Product updatedProduct = entityManager.find(Product.class, futureProduct.getId());
            assertThat(updatedProduct.getStock()).isEqualTo(stockBefore + 3);
        }
    }
}
