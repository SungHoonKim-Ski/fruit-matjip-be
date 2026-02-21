package store.onuljang.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.onuljang.shared.exception.NotFoundException;
import store.onuljang.shop.reservation.service.ReservationService;
import store.onuljang.shop.reservation.repository.ReservationAllRepository;
import store.onuljang.shop.reservation.repository.ReservationQueryRepository;
import store.onuljang.shop.reservation.repository.ReservationRepository;
import store.onuljang.shared.user.entity.*;
import store.onuljang.shop.product.entity.*;
import store.onuljang.shop.reservation.entity.*;
import store.onuljang.shop.delivery.entity.*;
import store.onuljang.shop.admin.entity.*;
import store.onuljang.shared.auth.entity.*;
import store.onuljang.shared.repository.entity.*;
import store.onuljang.shared.entity.enums.*;
import store.onuljang.shared.entity.base.*;
import store.onuljang.shared.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationQueryRepository reservationQueryRepository;

    @Mock
    private ReservationAllRepository reservationAllRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        testReservation = mock(Reservation.class);
    }

    @Test
    @DisplayName("findByIdWithLock - 예약 조회 성공")
    void findByIdWithLock_Success() {
        // given
        when(reservationQueryRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testReservation));

        // when
        Reservation result = reservationService.findByIdWithLock(1L);

        // then
        assertThat(result).isEqualTo(testReservation);
        verify(reservationQueryRepository).findByIdWithLock(1L);
    }

    @Test
    @DisplayName("findByIdWithLock - 존재하지 않는 예약 조회 시 예외 발생")
    void findByIdWithLock_NotFound_ThrowsException() {
        // given
        when(reservationQueryRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.findByIdWithLock(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    @DisplayName("save - 예약 저장 성공")
    void save_Success() {
        // given
        when(reservationRepository.save(testReservation)).thenReturn(testReservation);

        // when
        reservationService.save(testReservation);

        // then
        verify(reservationRepository).save(testReservation);
    }

    @Test
    @DisplayName("bulkUpdateReservationsStatus - 여러 예약 상태 일괄 업데이트")
    void bulkUpdateReservationsStatus_Success() {
        // given
        Set<Long> ids = Set.of(1L, 2L, 3L);
        LocalDateTime now = LocalDateTime.now();
        when(reservationQueryRepository.updateStatusIdIn(ids, ReservationStatus.PICKED, now)).thenReturn(3L);

        // when
        long result = reservationService.bulkUpdateReservationsStatus(ids, ReservationStatus.PICKED, now);

        // then
        assertThat(result).isEqualTo(3L);
        verify(reservationQueryRepository).updateStatusIdIn(ids, ReservationStatus.PICKED, now);
    }

    @Test
    @DisplayName("updateAllReservationsWhereIdIn - 조건부 상태 업데이트")
    void updateAllReservationsWhereIdIn_Success() {
        // given
        Set<Long> ids = Set.of(1L, 2L);
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        when(reservationQueryRepository.updateAllReservationStatus(ids, today, ReservationStatus.PENDING,
                ReservationStatus.NO_SHOW, now)).thenReturn(2L);

        // when
        long result = reservationService.updateAllReservationsWhereIdIn(ids, today, ReservationStatus.PENDING,
                ReservationStatus.NO_SHOW, now);

        // then
        assertThat(result).isEqualTo(2L);
    }

    @Test
    @DisplayName("findAllByIdInAndStatusGroupByProductIdOrderByProductId - 재고 복원 대상 조회")
    void findAllByIdInAndStatusGroupByProductIdOrderByProductId_Success() {
        // given
        Set<Long> ids = Set.of(1L, 2L);
        List<ProductRestockTarget> targets = List.of(mock(ProductRestockTarget.class));
        when(reservationQueryRepository.findAllByIdInAndStatusGroupByProductIdOrderByProductId(ids,
                ReservationStatus.CANCELED)).thenReturn(targets);

        // when
        List<ProductRestockTarget> result = reservationService
                .findAllByIdInAndStatusGroupByProductIdOrderByProductId(ids, ReservationStatus.CANCELED);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findAllUserIdInWithUserWithLock - 사용자 정보와 함께 예약 조회")
    void findAllUserIdInWithUserWithLock_Success() {
        // given
        Set<Long> ids = Set.of(1L, 2L);
        List<Reservation> reservations = List.of(testReservation);
        when(reservationQueryRepository.findAllByIdInWithUserWithLock(ids)).thenReturn(reservations);

        // when
        List<Reservation> result = reservationService.findAllUserIdInWithUserWithLock(ids);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findById - 예약 조회 성공")
    void findById_Success() {
        // given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        // when
        Reservation result = reservationService.findById(1L);

        // then
        assertThat(result).isEqualTo(testReservation);
    }

    @Test
    @DisplayName("findById - 존재하지 않는 예약 조회 시 예외 발생")
    void findById_NotFound_ThrowsException() {
        // given
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    @DisplayName("finAllByDateWithUserAndProduct - 날짜별 예약 조회")
    void finAllByDateWithUserAndProduct_Success() {
        // given
        LocalDate date = LocalDate.now();
        List<Reservation> reservations = List.of(testReservation);
        when(reservationRepository.findAllByPickupDate(date)).thenReturn(reservations);

        // when
        List<Reservation> result = reservationService.finAllByDateWithUserAndProduct(date);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findAllByUserAndOrderDateBetweenWithProductOrderByOrderDate - 사용자 예약 목록 조회")
    void findAllByUserAndOrderDateBetweenWithProductOrderByOrderDate_Success() {
        // given
        Users user = mock(Users.class);
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(7);
        List<Reservation> reservations = List.of(testReservation);
        when(reservationQueryRepository.findAllByUserAndPickupDateBetweenWithProductAllAndDelivery(user, from, to))
                .thenReturn(reservations);

        // when
        List<Reservation> result = reservationService
                .findAllByUserAndPickupDateBetweenWithProductAllAndDeliveryOrderByPickupDateDesc(user, from, to);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findPickupDateSales - 특정 날짜 판매 현황 조회")
    void findPickupDateSales_Success() {
        // given
        Set<ReservationStatus> statuses = Set.of(ReservationStatus.PICKED);
        LocalDate date = LocalDate.now();
        List<ReservationSalesRow> salesRows = List.of(mock(ReservationSalesRow.class));
        when(reservationAllRepository.findPickupDateSales(anyList(), eq(date))).thenReturn(salesRows);

        // when
        List<ReservationSalesRow> result = reservationService.findPickupDateSales(statuses, date);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findAllByPickupDateAndStatus - 경고 대상 조회")
    void findAllByPickupDateAndStatus_Success() {
        // given
        LocalDate today = LocalDate.now();
        List<ReservationWarnTarget> targets = List.of(mock(ReservationWarnTarget.class));
        when(reservationQueryRepository.findWarnTargetsByPickupDateAndStatus(today, ReservationStatus.PENDING))
                .thenReturn(targets);

        // when
        List<ReservationWarnTarget> result = reservationService.findAllByPickupDateAndStatus(today,
                ReservationStatus.PENDING);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findUserSalesRollbackTargets - 사용자 판매 롤백 대상 조회")
    void findUserSalesRollbackTargets_Success() {
        // given
        Set<Long> ids = Set.of(1L, 2L);
        List<UserSalesRollbackTarget> targets = List.of(mock(UserSalesRollbackTarget.class));
        when(reservationQueryRepository.findUserSalesRollbackTargets(ids, ReservationStatus.CANCELED))
                .thenReturn(targets);

        // when
        List<UserSalesRollbackTarget> result = reservationService.findUserSalesRollbackTargets(ids,
                ReservationStatus.CANCELED);

        // then
        assertThat(result).hasSize(1);
    }
}
