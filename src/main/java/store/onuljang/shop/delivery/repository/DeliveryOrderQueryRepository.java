package store.onuljang.shop.delivery.repository;

import static store.onuljang.shop.delivery.entity.QDeliveryOrder.deliveryOrder;
import static store.onuljang.shop.delivery.entity.QDeliveryOrderReservation.deliveryOrderReservation;
import static store.onuljang.shop.product.entity.QProductAll.productAll;
import static store.onuljang.shop.reservation.entity.QReservation.reservation;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shared.entity.enums.DeliveryStatus;

@Repository
@RequiredArgsConstructor
public class DeliveryOrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<DeliveryOrder> findAllByDeliveryDateWithProductAll(LocalDate deliveryDate) {
        return queryFactory
            .selectFrom(deliveryOrder)
            .leftJoin(deliveryOrder.user).fetchJoin()
            .leftJoin(deliveryOrder.deliveryOrderReservations, deliveryOrderReservation).fetchJoin()
            .leftJoin(deliveryOrderReservation.reservation, reservation).fetchJoin()
            .leftJoin(reservation.productAll, productAll).fetchJoin()
            .where(
                deliveryOrder.deliveryDate.eq(deliveryDate),
                deliveryOrder.status.notIn(DeliveryStatus.PENDING_PAYMENT, DeliveryStatus.FAILED)
            )
            .fetch();
    }
}
