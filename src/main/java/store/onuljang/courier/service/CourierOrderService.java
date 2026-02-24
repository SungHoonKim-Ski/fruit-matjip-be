package store.onuljang.courier.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.repository.CourierOrderRepository;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.exception.NotFoundException;
import store.onuljang.shared.user.entity.Users;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierOrderService {

    CourierOrderRepository courierOrderRepository;
    CourierPaymentService courierPaymentService;

    @Transactional
    public CourierOrder save(CourierOrder order) {
        return courierOrderRepository.save(order);
    }

    public CourierOrder findById(long id) {
        return courierOrderRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 택배 주문입니다."));
    }

    public CourierOrder findByDisplayCode(String displayCode) {
        return courierOrderRepository
                .findByDisplayCode(displayCode)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 택배 주문입니다."));
    }

    public CourierOrder findByDisplayCodeAndUser(String displayCode, Users user) {
        return courierOrderRepository
                .findByDisplayCodeAndUser(displayCode, user)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 택배 주문입니다."));
    }

    @Transactional
    public CourierOrder findByIdForUpdate(Long id) {
        return courierOrderRepository
                .findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 택배 주문입니다."));
    }

    public List<CourierOrder> findByUserAndDateRange(Users user, LocalDateTime start, LocalDateTime end) {
        return courierOrderRepository.findByUserAndDateRange(user, start, end);
    }

    public Optional<CourierOrder> findByIdempotencyKey(Users user, String key) {
        return courierOrderRepository.findByUserAndIdempotencyKey(user, key);
    }

    public boolean existsByDisplayCode(String displayCode) {
        return courierOrderRepository.existsByDisplayCode(displayCode);
    }

    public List<CourierOrder> findPendingPaymentsByUser(Users user) {
        return courierOrderRepository.findByUserAndStatus(
                user, CourierOrderStatus.PENDING_PAYMENT);
    }

    public CourierOrder findByIdWithItems(long id) {
        return courierOrderRepository
                .findByIdWithItems(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 택배 주문입니다."));
    }

    public Page<CourierOrder> findAllByStatus(CourierOrderStatus status, int page, int size) {
        return courierOrderRepository.findAllByStatusOrderByIdDesc(
                status, PageRequest.of(page, size));
    }

    public List<CourierOrder> findAllByIds(List<Long> ids) {
        return courierOrderRepository.findAllByIdIn(ids);
    }

    public List<CourierOrder> findByDateRangeAndStatuses(
            LocalDateTime startDateTime, LocalDateTime endDateTime,
            List<CourierOrderStatus> statuses) {
        return courierOrderRepository.findByDateRangeAndStatuses(startDateTime, endDateTime, statuses);
    }

    public List<CourierOrder> findByDateRangeAndStatusesAndProduct(
            LocalDateTime startDateTime, LocalDateTime endDateTime,
            List<CourierOrderStatus> statuses, Long productId) {
        return courierOrderRepository.findByDateRangeAndStatusesAndProduct(
                startDateTime, endDateTime, statuses, productId);
    }

    @Transactional
    public void completePaid(long orderId, String pgTid, String approveAid) {
        CourierOrder order = findById(orderId);
        if (!order.canMarkPaid()) return;
        order.markPaid(pgTid);
        courierPaymentService.markApproved(order, approveAid);
    }
}
