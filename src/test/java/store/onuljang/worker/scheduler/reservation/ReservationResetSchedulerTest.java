package store.onuljang.worker.scheduler.reservation;

import org.junit.jupiter.api.AfterEach;
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
import store.onuljang.shop.reservation.repository.ReservationRepository;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.product.entity.Product;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.entity.enums.ReservationStatus;
import store.onuljang.shared.util.TimeUtil;
import store.onuljang.support.TestFixture;

import java.math.BigDecimal;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReservationResetScheduler 테스트
 *
 * 10분 폴링 + guard check: pickupDeadline + 3분 이후에만 실행
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
    private LocalDate today;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        user = testFixture.createUser("테스트유저");
        // 고정 시각: 2026-01-15 20:30 (기본 pickupDeadline 20:00 + 3분 이후)
        today = LocalDate.of(2026, 1, 15);
        ZonedDateTime fixed = ZonedDateTime.of(today, LocalTime.of(20, 30), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(fixed.toInstant(), TimeUtil.KST));
    }

    @AfterEach
    void tearDown() {
        TimeUtil.resetClock();
    }

    @Test
    @DisplayName("노쇼 배치 처리 - PENDING 상태 예약을 NO_SHOW로 변경")
    void processNoShowBatch_Success() {
        // given - 오늘 상품의 예약 (PENDING, PICKED, CANCELED)
        Product product = testFixture.createProduct("테스트상품", 10, new BigDecimal("10000"), today, admin);

        Reservation pendingReservation = testFixture.createReservation(user, product, 2);
        Reservation pickedReservation = testFixture.createReservationWithStatus(user, product, 1,
                ReservationStatus.PICKED);
        Reservation canceledReservation = testFixture.createReservationWithStatus(user, product, 1,
                ReservationStatus.CANCELED);

        assertThat(pendingReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);

        // when
        reservationResetScheduler.processNoShowBatch();

        entityManager.clear();

        // then
        Reservation updatedPending = reservationRepository.findById(pendingReservation.getId()).orElseThrow();
        Reservation updatedPicked = reservationRepository.findById(pickedReservation.getId()).orElseThrow();
        Reservation updatedCanceled = reservationRepository.findById(canceledReservation.getId()).orElseThrow();

        assertThat(updatedPending.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
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
    void processNoShowBatch_FutureReservationsNotAffected() {
        // given - 내일 상품의 예약
        Product futureProduct = testFixture.createProduct("내일상품", 10, new BigDecimal("10000"),
                today.plusDays(1), admin);
        Reservation futureReservation = testFixture.createReservation(user, futureProduct, 2);

        // when
        reservationResetScheduler.processNoShowBatch();

        // then - 미래 예약은 변경 없음
        Reservation updatedReservation = reservationRepository.findById(futureReservation.getId()).orElseThrow();
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("마감 전 guard check - 수령 마감 전에는 노쇼 처리 안함")
    void processNoShowBatch_BeforeDeadline_Skips() {
        // given - 시각을 19:00으로 변경 (pickupDeadline 20:00 전)
        ZonedDateTime before = ZonedDateTime.of(today, LocalTime.of(19, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(before.toInstant(), TimeUtil.KST));

        Product product = testFixture.createProduct("테스트상품", 10, new BigDecimal("10000"), today, admin);
        Reservation reservation = testFixture.createReservation(user, product, 2);

        // when
        reservationResetScheduler.processNoShowBatch();

        entityManager.clear();

        // then - guard에 의해 skip되어 PENDING 유지
        Reservation updated = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("마감 직후 guard check - 수령 마감 + 3분 버퍼 직전이면 skip")
    void processNoShowBatch_WithinBuffer_Skips() {
        // given - 시각을 20:02로 변경 (pickupDeadline 20:00 + 3분 전)
        ZonedDateTime withinBuffer = ZonedDateTime.of(today, LocalTime.of(20, 2), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(withinBuffer.toInstant(), TimeUtil.KST));

        Product product = testFixture.createProduct("테스트상품", 10, new BigDecimal("10000"), today, admin);
        Reservation reservation = testFixture.createReservation(user, product, 2);

        // when
        reservationResetScheduler.processNoShowBatch();

        entityManager.clear();

        // then - 3분 버퍼 내이므로 skip
        Reservation updated = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("마감 + 3분 이후 guard check - 정상 실행")
    void processNoShowBatch_AfterBuffer_Processes() {
        // given - 시각을 20:04로 변경 (pickupDeadline 20:00 + 3분 이후)
        ZonedDateTime afterBuffer = ZonedDateTime.of(today, LocalTime.of(20, 4), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(afterBuffer.toInstant(), TimeUtil.KST));

        Product product = testFixture.createProduct("테스트상품", 10, new BigDecimal("10000"), today, admin);
        Reservation reservation = testFixture.createReservation(user, product, 2);

        // when
        reservationResetScheduler.processNoShowBatch();

        entityManager.clear();

        // then - 정상 처리
        Reservation updated = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
    }
}
