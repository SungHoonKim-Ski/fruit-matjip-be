package store.onuljang.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.Product;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Product, Long> {
    List<Product> findAllBySellDateBetween(LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"productDetailImages"})
    Optional<Product> findById(long id);
}

