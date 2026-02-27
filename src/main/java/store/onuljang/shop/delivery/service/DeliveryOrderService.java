package store.onuljang.shop.delivery.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import store.onuljang.shop.delivery.event.DeliveryPaidEvent;
import store.onuljang.shared.exception.NotFoundException;
import store.onuljang.shop.delivery.repository.DeliveryOrderRepository;
import store.onuljang.shop.delivery.repository.DeliveryOrderReservationRepository;
import store.onuljang.shop.delivery.repository.DeliveryOrderQueryRepository;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shop.delivery.entity.DeliveryOrderReservation;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.user.entity.Users;

import store.onuljang.shared.entity.enums.DeliveryStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
public class DeliveryOrderService {
    DeliveryOrderRepository deliveryOrderRepository;
    DeliveryOrderReservationRepository deliveryOrderReservationRepository;
    DeliveryOrderQueryRepository deliveryOrderQueryRepository;
    DeliveryPaymentService deliveryPaymentService;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    public DeliveryOrder save(DeliveryOrder order) {
        return deliveryOrderRepository.save(order);
    }

    @Transactional
    public List<DeliveryOrderReservation> saveLinks(List<DeliveryOrderReservation> links) {
        return deliveryOrderReservationRepository.saveAll(links);
    }

    public void deleteLinks(List<DeliveryOrderReservation> links) {
        deliveryOrderReservationRepository.deleteAll(links);
    }

    public Optional<DeliveryOrder> findByReservation(Reservation reservation) {
        return deliveryOrderReservationRepository.findByReservation(reservation)
            .map(DeliveryOrderReservation::getDeliveryOrder);
    }

    public DeliveryOrderReservation findLinkByReservation(Reservation reservation) {
        return deliveryOrderReservationRepository.findByReservation(reservation).orElse(null);
    }

    public DeliveryOrder findByIdAndUser(Long id, Users user) {
        return deliveryOrderRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 배달 주문입니다."));
    }

    public DeliveryOrder findByDisplayCodeAndUser(String displayCode, Users user) {
        return deliveryOrderRepository.findByDisplayCodeAndUser(displayCode, user)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 배달 주문입니다."));
    }

    public DeliveryOrder findById(long id) {
        return deliveryOrderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 배달 주문입니다."));
    }

    public List<DeliveryOrder> findAllByDeliveryDateWithProductAll(LocalDate date) {
        return deliveryOrderQueryRepository.findAllByDeliveryDateWithProductAll(date);
    }

    public DeliveryOrder findByUserAndIdempotencyKey(Users user, String idempotencyKey) {
        return deliveryOrderRepository.findByUserAndIdempotencyKey(user, idempotencyKey)
            .orElse(null);
    }

    public List<DeliveryOrder> findExpiredPendingPayments(LocalDateTime before) {
        return deliveryOrderRepository.findByStatusAndCreatedAtBefore(DeliveryStatus.PENDING_PAYMENT, before);
    }

    public List<DeliveryOrder> findPendingPaymentsWithTid(LocalDateTime before) {
        return deliveryOrderRepository.findByStatusAndKakaoTidIsNotNullAndCreatedAtBefore(
                DeliveryStatus.PENDING_PAYMENT, before);
    }

    public List<DeliveryOrder> findPendingPaymentsByUser(Users user) {
        return deliveryOrderRepository.findByUserAndStatus(user, DeliveryStatus.PENDING_PAYMENT);
    }

    public boolean existsByDisplayCode(String displayCode) {
        return deliveryOrderRepository.existsByDisplayCode(displayCode);
    }

    public List<DeliveryOrder> findOutForDeliveryBefore(LocalDateTime cutoff) {
        return deliveryOrderRepository.findByStatusAndAcceptedAtBefore(DeliveryStatus.OUT_FOR_DELIVERY, cutoff);
    }

    public List<DeliveryOrder> findActiveByDeliveryDate(LocalDate deliveryDate) {
        return deliveryOrderRepository.findByDeliveryDateAndStatusIn(
                deliveryDate, List.of(DeliveryStatus.PAID, DeliveryStatus.OUT_FOR_DELIVERY));
    }

    @Transactional
    public void completePaid(long orderId, String approveAid) {
        DeliveryOrder order = findById(orderId);
        if (!order.canMarkPaid()) return;
        order.markPaid();
        deliveryPaymentService.markApproved(order, approveAid);
        markReservationsPicked(order);
        eventPublisher.publishEvent(new DeliveryPaidEvent(orderId));
    }

    private void markReservationsPicked(DeliveryOrder order) {
        order.getReservations().stream()
            .filter(r -> r.getStatus() == store.onuljang.shared.entity.enums.ReservationStatus.PENDING)
            .forEach(r -> r.changeStatus(store.onuljang.shared.entity.enums.ReservationStatus.PICKED));
    }
}
