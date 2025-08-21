package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.exception.NotFoundException;
import store.onuljang.exception.UserValidateException;
import store.onuljang.repository.ReservationAllRepository;
import store.onuljang.repository.ReservationRepository;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.ReservationAll;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class ReservationService {
    ReservationRepository reservationRepository;
    ReservationAllRepository reservationAllRepository;

    @Transactional
    public Reservation findByIdWithLock(long id) {
        return reservationRepository.findByIdWithLock(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));
    }

    @Transactional
    public long save(Reservation reservation) {
        reservationRepository.save(reservation);

        return reservation.getId();
    }

    @Transactional
    public List<Reservation> finAllByDateWithUserAndProduct(LocalDate date) {
        return reservationRepository.findAllByPickupDate(date);
    }

    @Transactional
    public List<Reservation> findAllByUserAndOrderDateBetweenWithProductOrderByOrderDate(Users user, LocalDate from, LocalDate to) {
        return reservationRepository.findAllByUserAndPickupDateBetweenOrderByPickupDateDesc(user, from, to);
    }

    @Transactional(readOnly = true)
    public List<ReservationAll> findAllByStatusInAndPickupDateBetweenIncludingDeleted(List<ReservationStatus> statusList, LocalDate from, LocalDate to) {
        return reservationAllRepository.findAllByStatusInAndPickupDateBetweenOrderByPickupDateDesc(statusList ,from, to);
    }
}
