package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.exception.NotFoundException;
import store.onuljang.repository.DeliveryOrderRepository;
import store.onuljang.repository.DeliveryOrderReservationRepository;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.DeliveryOrderReservation;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
public class DeliveryOrderService {
    DeliveryOrderRepository deliveryOrderRepository;
    DeliveryOrderReservationRepository deliveryOrderReservationRepository;

    @Transactional
    public DeliveryOrder save(DeliveryOrder order) {
        return deliveryOrderRepository.save(order);
    }

    @Transactional
    public List<DeliveryOrderReservation> saveLinks(List<DeliveryOrderReservation> links) {
        return deliveryOrderReservationRepository.saveAll(links);
    }

    public Optional<DeliveryOrder> findByReservation(Reservation reservation) {
        return deliveryOrderReservationRepository.findByReservation(reservation)
            .map(DeliveryOrderReservation::getDeliveryOrder);
    }

    public DeliveryOrder findByIdAndUser(Long id, Users user) {
        return deliveryOrderRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 배달 주문입니다."));
    }

    @Transactional
    public DeliveryOrder findByIdWithLock(long id) {
        return deliveryOrderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 배달 주문입니다."));
    }

    public List<DeliveryOrder> findAllByDeliveryDate(LocalDate date) {
        return deliveryOrderRepository.findAllByDeliveryDate(date);
    }

    public List<DeliveryOrderReservation> findAllLinksByReservationIds(Set<Long> reservationIds) {
        return deliveryOrderReservationRepository.findAllByReservationIdIn(reservationIds);
    }

    public List<DeliveryOrderReservation> findAllLinksByDeliveryOrderId(Long deliveryOrderId) {
        return deliveryOrderReservationRepository.findAllByDeliveryOrderId(deliveryOrderId);
    }
}
