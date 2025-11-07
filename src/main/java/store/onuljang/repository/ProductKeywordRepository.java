package store.onuljang.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.ProductKeyword;

import java.util.List;

public interface ProductKeywordRepository extends JpaRepository<ProductKeyword, Long> {

    List<ProductKeyword> findAllByOrderByCreatedAtAsc();

    List<ProductKeyword> findAllByName(String name);

    int deleteByName(String name);
}

