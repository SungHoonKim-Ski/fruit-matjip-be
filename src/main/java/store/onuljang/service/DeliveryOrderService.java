package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import store.onuljang.event.delivery.DeliveryPaidEvent;
import store.onuljang.exception.NotFoundException;
import store.onuljang.repository.DeliveryOrderRepository;
import store.onuljang.repository.DeliveryOrderReservationRepository;
import store.onuljang.repository.DeliveryOrderQueryRepository;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.DeliveryOrderReservation;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;

import store.onuljang.repository.entity.enums.DeliveryStatus;

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

    public List<DeliveryOrder> findPendingPaymentsByUser(Users user) {
        return deliveryOrderRepository.findByUserAndStatus(user, DeliveryStatus.PENDING_PAYMENT);
    }

    public List<DeliveryOrder> findOutForDeliveryBefore(LocalDateTime cutoff) {
        return deliveryOrderRepository.findByStatusAndAcceptedAtBefore(DeliveryStatus.OUT_FOR_DELIVERY, cutoff);
    }

    @Transactional
    public void completePaid(long orderId, String approveAid) {
        DeliveryOrder order = findById(orderId);
        if (!order.canMarkPaid()) return;
        order.markPaid();
        deliveryPaymentService.markApproved(order, approveAid);
        eventPublisher.publishEvent(new DeliveryPaidEvent(orderId));
    }
}
