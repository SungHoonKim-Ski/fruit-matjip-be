package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.repository.entity.ReservationSalesRow;
import store.onuljang.repository.entity.ReservationAll;

import java.time.LocalDate;
import java.util.List;

public interface ReservationAllRepository extends JpaRepository<ReservationAll, Long> {
    @Query(value = """
        select
            r.product_id as productId,
            p.name as productName,
            SUM(r.quantity) as quantity,
            SUM(r.amount) as amount
        from reservations r
            join products p on p.id = r.product_id
        where r.pickup_date = :date
        and r.status in (:status)
        group by r.product_id
        order by p.name
    """, nativeQuery = true)
    List<ReservationSalesRow> findPickupDateSales(@Param("status") List<String> status, @Param("date") LocalDate date);
}
