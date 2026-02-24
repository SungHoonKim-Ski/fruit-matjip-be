package store.onuljang.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.TestS3Config;
import store.onuljang.config.TestSqsConfig;
import store.onuljang.shop.reservation.scheduler.ReservationResetScheduler;
import store.onuljang.shop.reservation.repository.ReservationRepository;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.product.entity.Product;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.entity.enums.ReservationStatus;
import store.onuljang.support.TestFixture;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReservationResetScheduler 테스트
 *
 * 매일 20:03에 당일 PENDING 상태 예약을 NO_SHOW로 처리
 */
@SpringBootTest
@ActiveProfiles("test")
@Import({TestS3Config.class, TestSqsConfig.class})
@Transactional
class ReservationResetSchedulerTest {

    @Autowired
    private ReservationResetScheduler reservationResetScheduler;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestFixture testFixture;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Admin admin;
    private Users user;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        user = testFixture.createUser("테스트유저");
    }

    @Test
    @DisplayName("노쇼 배치 처리 - PENDING 상태 예약을 NO_SHOW로 변경")
    void processNoShowBatch_Success() throws Exception {
        // given - 오늘 상품의 예약 (PENDING, PICKED, CANCELED)
        java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));
        Product product = testFixture.createProduct("테스트상품", 10, new BigDecimal("10000"), today, admin);

        Reservation pendingReservation = testFixture.createReservation(user, product, 2);
        Reservation pickedReservation = testFixture.createReservationWithStatus(user, product, 1,
                ReservationStatus.PICKED);
        Reservation canceledReservation = testFixture.createReservationWithStatus(user, product, 1,
                ReservationStatus.CANCELED);

        assertThat(pendingReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);

        // when
        reservationResetScheduler.processNoShowBatch();

        entityManager.clear(); // Bulk Update 반영 확인을 위해 영속성 컨텍스트 초기화

        // then
        Reservation updatedPending = reservationRepository.findById(pendingReservation.getId()).orElseThrow();
        Reservation updatedPicked = reservationRepository.findById(pickedReservation.getId()).orElseThrow();
        Reservation updatedCanceled = reservationRepository.findById(canceledReservation.getId()).orElseThrow();

        // PENDING만 NO_SHOW로 변경
        assertThat(updatedPending.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
        // PICKED, CANCELED는 변경 없음
        assertThat(updatedPicked.getStatus()).isEqualTo(ReservationStatus.PICKED);
        assertThat(updatedCanceled.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("예약이 없는 경우에도 정상 동작")
    void processNoShowBatch_NoReservations() {
        // when & then (예외 발생하지 않음)
        reservationResetScheduler.processNoShowBatch();
    }

    @Test
    @DisplayName("미래 상품의 예약은 노쇼 처리 대상 아님")
    void processNoShowBatch_FutureReservationsNotAffected() throws Exception {
        // given - 내일 상품의 예약
        Product futureProduct = testFixture.createFutureProduct("내일상품", 10, new BigDecimal("10000"), 1, admin);
        Reservation futureReservation = testFixture.createReservation(user, futureProduct, 2);

        // when
        reservationResetScheduler.processNoShowBatch();

        // then - 미래 예약은 변경 없음
        Reservation updatedReservation = reservationRepository.findById(futureReservation.getId()).orElseThrow();
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }
}
