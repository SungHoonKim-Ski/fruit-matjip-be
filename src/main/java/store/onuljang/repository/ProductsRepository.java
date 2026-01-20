package store.onuljang.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import store.onuljang.repository.entity.Product;

import java.time.LocalDate;
import java.util.*;

public interface ProductsRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {"productOrder"})
    List<Product> findAllBySellDateBetweenAndVisible(LocalDate from, LocalDate to, boolean visible);

    @EntityGraph(attributePaths = {"productOrder"})
    List<Product> findAllByOrderBySellDateDesc();

    List<Product> findAllByIdIn(Collection<Long> ids);

    @EntityGraph(attributePaths = {"productDetailImages"})
    Optional<Product> findAllById(long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "select p " +
        "from Product p " +
        "where p.id = :id"
    )
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "select p " +
        "from Product p " +
        "left join p.productDetailImages d " +
        "where p.id = :id"
    )
    Optional<Product> findAllByIdWithLock(long id);

    @Modifying(clearAutomatically = true)
    @Query("""
        update Product p
        set p.sellDate = :sellDate
        where p.id in(:ids)
    """)
    int bulkUpdateSellDateIdIn(@Param("ids") List<Long> ids, @Param("sellDate") LocalDate sellDate);


    @EntityGraph(attributePaths = {"productOrder"})
    @Query("""
                select distinct p from Product p
                join p.productCategories c
                where p.sellDate between :from and :to
                and p.visible = :visible
                and c.id = :categoryId
            """)
    List<Product> findAllBySellDateBetweenAndVisibleAndCategoryId(@Param("from") LocalDate from,
            @Param("to") LocalDate to, @Param("visible") boolean visible, @Param("categoryId") Long categoryId);

    @Query("select p from Product p join p.productCategories c where c.id = :categoryId")
    List<Product> findAllByCategoryId(@Param("categoryId") Long categoryId);
}
