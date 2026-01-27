package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.DeliveryInfoRequest;
import store.onuljang.controller.request.DeliveryReadyRequest;
import store.onuljang.controller.response.DeliveryInfoResponse;
import store.onuljang.controller.response.DeliveryReadyResponse;
import store.onuljang.event.delivery.DeliveryPaidEvent;
import store.onuljang.exception.UserNoContentException;
import store.onuljang.exception.UserValidateException;
import store.onuljang.feign.dto.request.KakaoPayApproveRequest;
import store.onuljang.feign.dto.reseponse.KakaoPayApproveResponse;
import store.onuljang.repository.entity.*;
import store.onuljang.repository.entity.enums.DeliveryStatus;
import store.onuljang.service.*;
import store.onuljang.validator.DeliveryValidator;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    KakaoLocalService kakaoLocalService;
    DeliveryValidator deliveryValidator;
    DeliveryFeeCalculator deliveryFeeCalculator;
    DeliveryPaymentProcessor deliveryPaymentProcessor;
    ApplicationEventPublisher eventPublisher;

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
        upsertDeliveryInfo(user, request.phone(), request.postalCode(), request.address1(), request.address2(),
            0.0, 0.0);
    }

    @Transactional
    public DeliveryReadyResponse ready(String uid, DeliveryReadyRequest request) {
        // 1) 사용자/예약 조회 및 기본 검증
        Users user = userService.findByUId(uid);
        List<Reservation> reservations = loadReservations(request);

        // 2) 배달 가능 여부 및 시간 검증
        deliveryValidator.validateReservations(user, reservations);
        deliveryValidator.validateDeliveryTime(request.deliveryHour(), request.deliveryMinute());

        // 3) 주소 → 좌표 변환 및 배달비 산정
        KakaoLocalService.Coordinate coordinate = kakaoLocalService.geocodeAddress(request.address1());
        DeliveryFeeCalculator.FeeResult feeResult = deliveryFeeCalculator.calculate(coordinate.latitude(), coordinate.longitude());
        BigDecimal totalProductAmount = calculateTotalProductAmount(reservations);
        deliveryValidator.validateMinimumAmount(totalProductAmount);

        // 4) 배송 정보 저장 및 주문 생성/연결
        saveDeliveryInfo(user, request, coordinate);
        DeliveryOrder saved = createDeliveryOrder(user, request, reservations, coordinate, feeResult);
        saveDeliveryOrderLinks(saved, reservations);

        // 5) 결제 준비 (카카오페이 or 즉시 결제 처리)
        return deliveryPaymentProcessor.preparePayment(saved, user, reservations, totalProductAmount, feeResult.deliveryFee());
    }

    @Transactional
    public void approve(String uid, long orderId, String pgToken) {
        Users user = userService.findByUId(uid);
        DeliveryOrder order = deliveryOrderService.findByIdAndUser(orderId, user);

        if (order.getStatus() != DeliveryStatus.PENDING_PAYMENT) {
            throw new UserValidateException("결제 진행 상태가 아닙니다.");
        }

        KakaoPayApproveResponse approve = kakaoPayService.approve(
            new KakaoPayApproveRequest(
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
        eventPublisher.publishEvent(new DeliveryPaidEvent(order.getId()));
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

    // 예약 상품 총액 합산
    private BigDecimal calculateTotalProductAmount(List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 예약 목록 로드 및 존재 여부 검증
    private List<Reservation> loadReservations(DeliveryReadyRequest request) {
        Set<Long> reservationIdSet = new HashSet<>(request.reservationIds());
        List<Reservation> reservations = reservationService.findAllUserIdInWithUser(reservationIdSet);
        if (reservations.size() != request.reservationIds().size()) {
            throw new UserValidateException("존재하지 않는 예약이 포함되어 있습니다.");
        }
        return reservations;
    }

    // 사용자 배송 정보 저장/갱신
    private void saveDeliveryInfo(Users user, DeliveryReadyRequest request, KakaoLocalService.Coordinate coordinate) {
        upsertDeliveryInfo(user, request.phone(), request.postalCode(), request.address1(), request.address2(),
            coordinate.latitude(), coordinate.longitude());
    }

    private void upsertDeliveryInfo(Users user, String phone, String postalCode, String address1, String address2,
            Double latitude, Double longitude) {
        UserDeliveryInfo existing = userDeliveryInfoService.findByUser(user).orElse(null);
        if (existing != null) {
            userDeliveryInfoService.update(existing, phone, postalCode, address1, address2, latitude, longitude);
        } else {
            userDeliveryInfoService.create(user, phone, postalCode, address1, address2, latitude, longitude);
        }
    }

    // 배달 주문 생성 및 저장
    private DeliveryOrder createDeliveryOrder(Users user, DeliveryReadyRequest request, List<Reservation> reservations,
                  KakaoLocalService.Coordinate coordinate, DeliveryFeeCalculator.FeeResult feeResult) {

        return deliveryOrderService.save(DeliveryOrder.builder()
                .user(user)
                .status(DeliveryStatus.PENDING_PAYMENT)
                .deliveryDate(reservations.get(0).getPickupDate())
                .deliveryHour(request.deliveryHour())
                .deliveryMinute(request.deliveryMinute())
                .deliveryFee(feeResult.deliveryFee())
                .distanceKm(feeResult.distanceKm())
                .postalCode(request.postalCode())
                .address1(request.address1())
                .address2(request.address2())
                .phone(request.phone())
                .latitude(coordinate.latitude())
                .longitude(coordinate.longitude())
                .build());
    }

    // 배달 주문-예약 연결 저장
    private void saveDeliveryOrderLinks(DeliveryOrder order, List<Reservation> reservations) {
        List<DeliveryOrderReservation> links = reservations.stream()
                .map(reservation -> DeliveryOrderReservation.builder()
                        .deliveryOrder(order)
                        .reservation(reservation)
                        .build())
                .toList();
        deliveryOrderService.saveLinks(links);
        order.getDeliveryOrderReservations().addAll(links);
    }
}
