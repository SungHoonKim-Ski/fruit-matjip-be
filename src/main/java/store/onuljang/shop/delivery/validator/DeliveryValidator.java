package store.onuljang.shop.delivery.validator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shop.delivery.config.DeliveryConfigSnapshot;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.entity.enums.DeliveryStatus;
import store.onuljang.shared.entity.enums.ReservationStatus;
import store.onuljang.shop.delivery.service.DeliveryConfigService;
import store.onuljang.shop.delivery.service.DeliveryOrderService;
import store.onuljang.shop.delivery.util.MathUtil;
import store.onuljang.shared.util.TimeUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryValidator {
    DeliveryOrderService deliveryOrderService;
    DeliveryConfigService deliveryConfigService;

    public void validateReservations(Users user, List<Reservation> reservations) {
        if (user.isRestricted()) {
            java.time.LocalDate until = user.getRestrictedUntil().plusDays(1);
            throw new UserValidateException(
                    String.format("노쇼로 인해 서비스 이용이 제한됩니다. %d/%02d/%02d 이후부터 이용 가능합니다.",
                            until.getYear(), until.getMonthValue(), until.getDayOfMonth()));
        }
        DeliveryConfigSnapshot config = deliveryConfigService.getConfig();
        if (!config.enabled()) {
            throw new UserValidateException("현재 배달 주문이 중단되어 있습니다.");
        }
        LocalDate today = resolveCurrentBusinessDate(config);
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

        if (TimeUtil.isDeadlineOver(today, config.endHour(), config.endMinute())) {
            throw new UserValidateException("배달 주문 가능 시간이 지났습니다.");
        }
    }

    public void validateDeliveryTime(Integer deliveryHour, Integer deliveryMinute) {
        DeliveryConfigSnapshot config = deliveryConfigService.getConfig();
        int startHour = config.startHour();
        int startMinute = config.startMinute();
        int endHour = config.endHour();
        int endMinute = config.endMinute();
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
        DeliveryConfigSnapshot config = deliveryConfigService.getConfig();
        if (totalProductAmount.compareTo(config.minAmount()) < 0) {
            throw new UserValidateException("배달 주문은 " + MathUtil.formatAmount(config.minAmount())
                + "원 이상부터 가능합니다.");
        }
    }

    public void validateScheduledDelivery(Integer scheduledHour, Integer scheduledMinute) {
        if (scheduledHour == null) {
            return;
        }
        int safeMinute = scheduledMinute != null ? scheduledMinute : 0;
        DeliveryConfigSnapshot config = deliveryConfigService.getConfig();

        // 슬롯: (startHour:startMinute + 1h) ~ last valid slot within endHour:endMinute, 1시간 간격
        int startTotal = config.startHour() * 60 + config.startMinute();
        int endTotal = config.endHour() * 60 + config.endMinute();
        int minSlotTotal = startTotal + 60;
        int scheduledTotal = scheduledHour * 60 + safeMinute;

        // 접수 마감: endTotal - 60분 이후 접수 불가
        int cutoffTotal = endTotal - 60;
        LocalDateTime now = TimeUtil.nowDateTime();
        int currentTotal = now.getHour() * 60 + now.getMinute();
        if (config.endHour() >= 24) {
            int overflowMinutes = (config.endHour() - 24) * 60 + config.endMinute();
            if (currentTotal <= overflowMinutes) {
                currentTotal += 24 * 60;
            }
        }

        if (currentTotal >= cutoffTotal) {
            throw new UserValidateException(
                "예약배달은 " + TimeUtil.formatTime(cutoffTotal / 60, cutoffTotal % 60) + "까지만 접수 가능합니다.");
        }

        // 슬롯 유효성 검증 (startMinute과 동일한 분이어야 함)
        if (safeMinute != config.startMinute()) {
            throw new UserValidateException(
                "예약배달 시간은 " + config.startMinute() + "분 단위여야 합니다.");
        }

        // 슬롯 범위 검증: minSlot ~ endTotal 사이, 그리고 1시간 간격으로 유효한 슬롯
        if (scheduledTotal < minSlotTotal) {
            throw new UserValidateException(
                "예약배달 시간은 " + TimeUtil.formatTime(minSlotTotal / 60, minSlotTotal % 60)
                    + " 이후여야 합니다.");
        }
        if (scheduledTotal > endTotal) {
            throw new UserValidateException(
                "예약배달 시간은 " + TimeUtil.formatTime(endTotal / 60, endTotal % 60)
                    + " 이전이어야 합니다.");
        }

        // 최소 1시간 이후
        int remainingMinutes = scheduledTotal - currentTotal;
        if (remainingMinutes < 60) {
            throw new UserValidateException("예약배달은 현재 시간 기준 최소 1시간 이후 선택 가능합니다.");
        }
    }

    private void validateUserReservation(Users user, Reservation reservation) {
        if (!user.getUid().equals(reservation.getUser().getUid())) {
            throw new UserValidateException("다른 유저가 예약한 상품입니다.");
        }
    }

    /**
     * cross-midnight 대응: 자정 이후 배달 종료시각 전이면 전날 영업일 반환.
     */
    private LocalDate resolveCurrentBusinessDate(DeliveryConfigSnapshot config) {
        LocalDate today = TimeUtil.nowDate();
        if (config.endHour() >= 24) {
            int overflowMinutes = (config.endHour() - 24) * 60 + config.endMinute();
            ZonedDateTime now = TimeUtil.nowZonedDateTime();
            int nowTotal = now.getHour() * 60 + now.getMinute();
            if (nowTotal <= overflowMinutes) {
                return today.minusDays(1);
            }
        }
        return today;
    }
}
