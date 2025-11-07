package store.onuljang.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import store.onuljang.repository.entity.ProductKeyword;

import java.util.List;

public interface ProductKeywordRepository extends JpaRepository<ProductKeyword, Long> {

    List<ProductKeyword> findAllByOrderByCreatedAtAsc();

    List<ProductKeyword> findAllByName(String name);

    @Modifying(flushAutomatically = true)
    void deleteByName(String name);

    @Modifying(flushAutomatically = true)
    @Query("delete from ProductKeyword")
    void deleteAllKeywords();
}

