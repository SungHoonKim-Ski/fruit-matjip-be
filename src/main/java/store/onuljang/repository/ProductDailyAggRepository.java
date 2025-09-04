package store.onuljang.repository;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.repository.entity.ProductDailyAgg;
import store.onuljang.repository.entity.ProductDailyAggRow;

import java.time.LocalDate;
import java.util.List;

public interface ProductDailyAggRepository extends JpaRepository<ProductDailyAgg, Long> {
    @Modifying(flushAutomatically = true)
    @Query(value = """
        INSERT INTO product_daily_agg (sell_date, product_id, quantity, amount)
            SELECT
                r.pickup_date,
                r.product_id,
                SUM(CASE a.phase WHEN 'NO_SHOW_MINUS' THEN -r.quantity ELSE r.quantity END),
                SUM(CASE a.phase WHEN 'NO_SHOW_MINUS' THEN -r.amount   ELSE r.amount   END)
            FROM agg_applied a
            JOIN reservations r ON r.id = a.reservation_id
            WHERE a.batch_uid = :batchUid
            AND a.phase IN ('PICKED_PLUS','SELF_PICKUP_READY_PLUS','NO_SHOW_MINUS')
            GROUP BY r.pickup_date, r.product_id
        ON DUPLICATE KEY UPDATE
        quantity = quantity + VALUES(quantity),
        amount   = amount + VALUES(amount)
    """, nativeQuery = true)
    int upsertForBatch(@Param("batchUid") String batchUid);

    @Query(value = """
        SELECT
            a.sell_date AS sellDate,
            sum(a.quantity) AS quantity,
            SUM(a.amount) AS amount
        FROM product_daily_agg a
        WHERE a.sell_date BETWEEN :from AND :to
        GROUP BY a.sell_date
    """, nativeQuery = true)
    List<ProductDailyAggRow> findAggBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @EntityGraph(attributePaths = {"product"})
    List<ProductDailyAgg> findAllBySellDate(LocalDate sellDate);
}
