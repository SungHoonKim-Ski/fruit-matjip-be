package store.onuljang.shop.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.shop.delivery.entity.DeliveryDailyAgg;
import store.onuljang.shop.delivery.entity.DeliveryDailyAggRow;

import java.time.LocalDate;
import java.util.List;

public interface DeliveryDailyAggRepository extends JpaRepository<DeliveryDailyAgg, Long> {
    @Modifying(flushAutomatically = true)
    @Query(
            value =
                    """
                INSERT INTO delivery_daily_agg (sell_date, order_count, quantity, amount, delivery_fee)
                    SELECT
                        :sellDate,
                        COUNT(DISTINCT do2.id),
                        COALESCE(SUM(r.quantity), 0),
                        COALESCE(SUM(r.amount), 0),
                        (SELECT COALESCE(SUM(d.delivery_fee), 0)
                         FROM delivery_orders d
                         WHERE d.status = 'DELIVERED'
                         AND d.delivery_date = :sellDate)
                    FROM delivery_orders do2
                    JOIN delivery_order_reservations dor ON dor.delivery_order_id = do2.id
                    JOIN reservations r ON r.id = dor.reservation_id
                    WHERE do2.status = 'DELIVERED'
                    AND do2.delivery_date = :sellDate
                ON DUPLICATE KEY UPDATE
                    order_count = VALUES(order_count),
                    quantity = VALUES(quantity),
                    amount = VALUES(amount),
                    delivery_fee = VALUES(delivery_fee)
            """,
            nativeQuery = true)
    int upsertForDate(@Param("sellDate") LocalDate sellDate);

    @Query(
            value =
                    """
                SELECT
                    sell_date AS sellDate,
                    order_count AS orderCount,
                    quantity,
                    amount,
                    delivery_fee AS deliveryFee
                FROM delivery_daily_agg
                WHERE sell_date BETWEEN :from AND :to
                ORDER BY sell_date
            """,
            nativeQuery = true)
    List<DeliveryDailyAggRow> findAggBetween(
            @Param("from") LocalDate from, @Param("to") LocalDate to);
}
