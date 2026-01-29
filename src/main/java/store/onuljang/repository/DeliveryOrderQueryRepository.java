package store.onuljang.repository;

import static store.onuljang.repository.entity.QDeliveryOrder.deliveryOrder;
import static store.onuljang.repository.entity.QDeliveryOrderReservation.deliveryOrderReservation;
import static store.onuljang.repository.entity.QProductAll.productAll;
import static store.onuljang.repository.entity.QReservation.reservation;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store.onuljang.repository.entity.DeliveryOrder;

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
            .where(deliveryOrder.deliveryDate.eq(deliveryDate))
            .fetch();
    }
}
