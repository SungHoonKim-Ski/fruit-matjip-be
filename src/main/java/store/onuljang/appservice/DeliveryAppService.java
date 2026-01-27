package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.DeliveryConfigDto;
import store.onuljang.config.KakaoPayConfigDto;
import store.onuljang.controller.request.DeliveryInfoRequest;
import store.onuljang.controller.request.DeliveryReadyRequest;
import store.onuljang.controller.response.DeliveryInfoResponse;
import store.onuljang.controller.response.DeliveryReadyResponse;
import store.onuljang.exception.UserValidateException;
import store.onuljang.exception.UserNoContentException;
import store.onuljang.repository.entity.*;
import store.onuljang.repository.entity.enums.DeliveryPaymentStatus;
import store.onuljang.repository.entity.enums.DeliveryStatus;
import store.onuljang.repository.entity.enums.PaymentProvider;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.service.*;
import store.onuljang.util.TimeUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryAppService {
    ReservationService reservationService;
    UserService userService;
    UserDeliveryInfoService userDeliveryInfoService;
    DeliveryOrderService deliveryOrderService;
    DeliveryPaymentService deliveryPaymentService;
    KakaoPayService kakaoPayService;
    KakaoPayConfigDto kakaoPayConfigDto;
    KakaoLocalService kakaoLocalService;
    DeliveryConfigDto deliveryConfigDto;
    AdminDeliverySseService adminDeliverySseService;

    @Transactional(readOnly = true)
    public DeliveryInfoResponse getDeliveryInfo(String uid) {
        Users user = userService.findByUId(uid);
        return userDeliveryInfoService.findByUser(user)
            .map(DeliveryInfoResponse::from)
            .orElseThrow(() -> new UserNoContentException("배송 정보가 없습니다."));
    }

    @Transactional
    public void saveDeliveryInfo(String uid, DeliveryInfoRequest request) {
        Users user = userService.findByUidWithLock(uid);
        userDeliveryInfoService.saveOrUpdate(user, request.phone(), request.postalCode(), request.address1(),
            request.address2(), 0.0, 0.0);
    }

    @Transactional
    public DeliveryReadyResponse ready(String uid, DeliveryReadyRequest request) {
        Users user = userService.findByUidWithLock(uid);
        java.util.Set<Long> reservationIdSet = new java.util.HashSet<>(request.reservationIds());
        if (reservationIdSet.isEmpty()) {
            throw new UserValidateException("배달 주문 대상이 없습니다.");
        }
        java.util.List<Reservation> reservations = reservationService.findAllUserIdInWithUser(reservationIdSet);
        if (reservations.size() != reservationIdSet.size()) {
            throw new UserValidateException("존재하지 않는 예약이 포함되어 있습니다.");
        }

        validateDeliveryReservations(user, reservations);
        validateDeliveryTime(request.deliveryHour(), request.deliveryMinute());

        KakaoLocalService.Coordinate coordinate = kakaoLocalService.geocodeAddress(request.address1())
            .orElseThrow(() -> new UserValidateException("주소 좌표를 찾을 수 없습니다."));
        double distanceKmValue = calculateDistanceKm(deliveryConfigDto.getStoreLat(), deliveryConfigDto.getStoreLng(),
            coordinate.latitude(), coordinate.longitude());
        if (distanceKmValue > deliveryConfigDto.getMaxDistanceKm()) {
            throw new UserValidateException("배달 가능 거리(" + trimDistance(deliveryConfigDto.getMaxDistanceKm()) + "km)를 초과했습니다.");
        }
        BigDecimal distanceKm = BigDecimal.valueOf(distanceKmValue).setScale(3, RoundingMode.HALF_UP);
        BigDecimal deliveryFee;
        double baseDistance = deliveryConfigDto.getFeeDistanceKm();
        if (distanceKmValue <= baseDistance) {
            deliveryFee = deliveryConfigDto.getFeeNear();
        } else {
            double extraKm = Math.max(0, distanceKmValue - baseDistance);
            long extraUnits = (long) Math.ceil(extraKm / 0.1d);
            deliveryFee = deliveryConfigDto.getFeeNear()
                .add(deliveryConfigDto.getFeePer100m().multiply(BigDecimal.valueOf(extraUnits)));
        }

        BigDecimal totalProductAmount = reservations.stream()
            .map(Reservation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalProductAmount.compareTo(deliveryConfigDto.getMinAmount()) < 0) {
            throw new UserValidateException("배달 주문은 " + formatAmount(deliveryConfigDto.getMinAmount()) + "원 이상부터 가능합니다.");
        }

        userDeliveryInfoService.saveOrUpdate(user, request.phone(), request.postalCode(), request.address1(),
            request.address2(), coordinate.latitude(), coordinate.longitude());

        DeliveryOrder order = DeliveryOrder.builder()
            .user(user)
            .status(DeliveryStatus.PENDING_PAYMENT)
            .deliveryDate(reservations.get(0).getPickupDate())
            .deliveryHour(request.deliveryHour())
            .deliveryMinute(request.deliveryMinute())
            .deliveryFee(deliveryFee)
            .distanceKm(distanceKm)
            .postalCode(request.postalCode())
            .address1(request.address1())
            .address2(request.address2())
            .phone(request.phone())
            .latitude(coordinate.latitude())
            .longitude(coordinate.longitude())
            .build();

        DeliveryOrder saved = deliveryOrderService.save(order);
        java.util.List<DeliveryOrderReservation> links = reservations.stream()
            .map(reservation -> DeliveryOrderReservation.builder()
                .deliveryOrder(saved)
                .reservation(reservation)
                .build())
            .toList();
        deliveryOrderService.saveLinks(links);
        saved.getDeliveryOrderReservations().addAll(links);

        if (!kakaoPayConfigDto.isEnabled()) {
            saved.markPaid();
            adminDeliverySseService.notifyPaid(saved);
            return DeliveryReadyResponse.builder()
                .orderId(saved.getId())
                .redirectUrl("/me/orders")
                .build();
        }

        int totalAmount = totalProductAmount.add(deliveryFee).intValue();
        String approvalUrl = kakaoPayConfigDto.getApprovalUrl() + "?order_id=" + saved.getId();
        String cancelUrl = kakaoPayConfigDto.getCancelUrl() + "?order_id=" + saved.getId();
        String failUrl = kakaoPayConfigDto.getFailUrl() + "?order_id=" + saved.getId();

        KakaoPayService.KakaoPayReadyResponse ready = kakaoPayService.ready(
            new KakaoPayService.KakaoPayReadyRequest(
                String.valueOf(saved.getId()),
                user.getUid(),
                buildReservationTitle(reservations),
                reservations.stream().mapToInt(Reservation::getQuantity).sum(),
                totalAmount,
                approvalUrl,
                cancelUrl,
                failUrl
            )
        );

        saved.setKakaoTid(ready.tid());
        deliveryPaymentService.save(DeliveryPayment.builder()
            .deliveryOrder(saved)
            .pgProvider(PaymentProvider.KAKAOPAY)
            .status(DeliveryPaymentStatus.READY)
            .amount(BigDecimal.valueOf(totalAmount))
            .tid(ready.tid())
            .build());

        return DeliveryReadyResponse.builder()
            .orderId(saved.getId())
            .redirectUrl(ready.next_redirect_pc_url())
            .build();
    }

    @Transactional
    public void approve(String uid, long orderId, String pgToken) {
        Users user = userService.findByUId(uid);
        DeliveryOrder order = deliveryOrderService.findByIdAndUser(orderId, user);

        if (order.getStatus() != DeliveryStatus.PENDING_PAYMENT) {
            throw new UserValidateException("결제 진행 상태가 아닙니다.");
        }

        KakaoPayService.KakaoPayApproveResponse approve = kakaoPayService.approve(
            new KakaoPayService.KakaoPayApproveRequest(
                order.getKakaoTid(),
                String.valueOf(order.getId()),
                user.getUid(),
                pgToken
            )
        );

        if (approve == null) {
            order.markFailed();
            throw new UserValidateException("결제 승인에 실패했습니다.");
        }

        order.markPaid();
        deliveryPaymentService.findLatestByOrder(order)
            .ifPresent(payment -> payment.markApproved(approve.aid()));
        adminDeliverySseService.notifyPaid(order);
    }

    @Transactional
    public void cancel(String uid, long orderId) {
        Users user = userService.findByUId(uid);
        DeliveryOrder order = deliveryOrderService.findByIdAndUser(orderId, user);
        if (order.getStatus() == DeliveryStatus.PAID) {
            throw new UserValidateException("이미 결제 완료된 주문입니다.");
        }
        order.markCanceled();
        deliveryPaymentService.findLatestByOrder(order)
            .ifPresent(DeliveryPayment::markCanceled);
    }

    @Transactional
    public void fail(String uid, long orderId) {
        Users user = userService.findByUId(uid);
        DeliveryOrder order = deliveryOrderService.findByIdAndUser(orderId, user);
        if (order.getStatus() == DeliveryStatus.PAID) {
            throw new UserValidateException("이미 결제 완료된 주문입니다.");
        }
        order.markFailed();
        deliveryPaymentService.findLatestByOrder(order)
            .ifPresent(DeliveryPayment::markFailed);
    }

    private void validateUserReservation(Users user, Reservation reservation) {
        if (!user.getUid().equals(reservation.getUser().getUid())) {
            throw new UserValidateException("다른 유저가 예약한 상품입니다.");
        }
    }

    private void validateDeliveryReservations(Users user, java.util.List<Reservation> reservations) {
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

    private void validateDeliveryTime(Integer deliveryHour, Integer deliveryMinute) {
        int startHour = deliveryConfigDto.getStartHour();
        int startMinute = deliveryConfigDto.getStartMinute();
        int endHour = deliveryConfigDto.getEndHour();
        int endMinute = deliveryConfigDto.getEndMinute();
        if (deliveryHour == null || deliveryMinute == null) {
            throw new UserValidateException("배달 수령 시간을 확인해주세요.");
        }
        if (deliveryHour < startHour || (deliveryHour == startHour && deliveryMinute < startMinute)) {
            throw new UserValidateException("배달 수령 시간은 " + formatTime(startHour, startMinute)
                + "~" + formatTime(endHour, endMinute) + " 사이여야 합니다.");
        }
        if (deliveryHour > endHour || (deliveryHour == endHour && deliveryMinute > endMinute)) {
            throw new UserValidateException("배달 수령 시간은 " + formatTime(startHour, startMinute)
                + "~" + formatTime(endHour, endMinute) + " 사이여야 합니다.");
        }
    }

    private String formatTime(int hour, int minute) {
        if (minute <= 0) {
            return hour + "시";
        }
        return hour + "시 " + minute + "분";
    }

    private String buildReservationTitle(java.util.List<Reservation> reservations) {
        if (reservations.isEmpty()) return "배달 주문";
        String first = reservations.get(0).getReservationProductName();
        if (reservations.size() == 1) return first;
        return first + " 외 " + (reservations.size() - 1) + "건";
    }

    private String formatAmount(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }

    private String trimDistance(double distanceKm) {
        if (Math.floor(distanceKm) == distanceKm) {
            return String.valueOf((int) distanceKm);
        }
        return BigDecimal.valueOf(distanceKm).stripTrailingZeros().toPlainString();
    }

    private double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c;
    }
}
