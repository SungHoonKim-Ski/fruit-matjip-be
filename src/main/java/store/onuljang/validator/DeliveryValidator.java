package store.onuljang.validator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.DeliveryConfigDto;
import store.onuljang.exception.UserValidateException;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.DeliveryStatus;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.service.DeliveryOrderService;
import store.onuljang.util.MathUtil;
import store.onuljang.util.TimeUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryValidator {
    DeliveryOrderService deliveryOrderService;
    DeliveryConfigDto deliveryConfigDto;

    public void validateReservations(Users user, List<Reservation> reservations) {
        LocalDate today = TimeUtil.nowDate();
        LocalDate deliveryDate = reservations.get(0).getPickupDate();
        if (!deliveryDate.isEqual(today)) {
            throw new UserValidateException("배달 주문은 오늘 수령 예약만 가능합니다.");
        }
        for (Reservation reservation : reservations) {
            validateUserReservation(user, reservation);
            if (!reservation.getProduct().getSellDate().isEqual(today)) {
                throw new UserValidateException("판매일이 오늘인 상품만 배달 가능합니다.");
            }
            if (reservation.getStatus() != ReservationStatus.PENDING) {
                throw new UserValidateException("배달 주문이 불가능한 예약입니다.");
            }
            if (!reservation.getDeliveryAvailable()) {
                throw new UserValidateException("배달 불가 상품이 포함되어 있습니다.");
            }
            if (!reservation.getPickupDate().isEqual(deliveryDate)) {
                throw new UserValidateException("같은 수령일 예약만 묶을 수 있습니다.");
            }
            deliveryOrderService.findByReservation(reservation).ifPresent(existing -> {
                DeliveryStatus status = existing.getStatus();
                if (status != DeliveryStatus.CANCELED && status != DeliveryStatus.FAILED) {
                    throw new UserValidateException("이미 배달 주문이 진행 중인 예약이 포함되어 있습니다.");
                }
            });
        }

        LocalTime deadlineTime = LocalTime.of(deliveryConfigDto.getEndHour(), deliveryConfigDto.getEndMinute());
        if (TimeUtil.isAfterDeadline(today, deadlineTime)) {
            throw new UserValidateException("배달 주문 가능 시간이 지났습니다.");
        }
    }

    public void validateDeliveryTime(Integer deliveryHour, Integer deliveryMinute) {
        int startHour = deliveryConfigDto.getStartHour();
        int startMinute = deliveryConfigDto.getStartMinute();
        int endHour = deliveryConfigDto.getEndHour();
        int endMinute = deliveryConfigDto.getEndMinute();
        if (deliveryHour == null || deliveryMinute == null) {
            throw new UserValidateException("배달 수령 시간을 확인해주세요.");
        }
        if (TimeUtil.isBefore(deliveryHour, deliveryMinute, startHour, startMinute)
            || TimeUtil.isAfter(deliveryHour, deliveryMinute, endHour, endMinute)) {
            throw new UserValidateException("배달 수령 시간은 " + TimeUtil.formatTime(startHour, startMinute)
                + "~" + TimeUtil.formatTime(endHour, endMinute) + " 사이여야 합니다.");
        }
    }

    public void validateMinimumAmount(BigDecimal totalProductAmount) {
        if (totalProductAmount.compareTo(deliveryConfigDto.getMinAmount()) < 0) {
            throw new UserValidateException("배달 주문은 " + MathUtil.formatAmount(deliveryConfigDto.getMinAmount())
                + "원 이상부터 가능합니다.");
        }
    }

    private void validateUserReservation(Users user, Reservation reservation) {
        if (!user.getUid().equals(reservation.getUser().getUid())) {
            throw new UserValidateException("다른 유저가 예약한 상품입니다.");
        }
    }
}
