package store.onuljang.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.repository.entity.Product;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Product, Long> {
    List<Product> findAllBySellDateBetweenAndIsVisible(LocalDate sellDate, LocalDate sellDate2, boolean isVisible);

    List<Product> findAllByOrderBySellDateDesc();

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
}

