package store.onuljang.shared.user.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.shop.admin.dto.AdminCustomerSortKey;
import store.onuljang.shop.admin.dto.SortOrder;
import store.onuljang.shared.user.entity.Users;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findBySocialId(String socialId);
    Optional<Users> findByUid(String uid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "select u " +
        "from Users u " +
        "where u.uid = :uid"
    )
    Optional<Users> findByUidWithLock(String uid);
    Optional<Users> findByName(String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        "update Users u " +
        "set u.monthlyWarnCount = 0"
    )
    int resetAllUsersWarnCounts();

    @Query(value = """
            select
                *
            from users u
            where (:name is null or :name = '' or u.name like concat('%', :name, '%'))
            order by
                case when :sortKey = 'TOTAL_REVENUE' then u.total_revenue end :sortOrder,
                case when :sortKey = 'TOTAL_WARN_COUNT' then u.total_warn_count end :sortOrder,
                case when :sortKey = 'WARN_COUNT' then u.warn_count end :sortOrder,
                u.id asc
            limit :limit offset :offset
            """, nativeQuery = true)
    List<Users> getUsers(@Param("name") String name, @Param("sortKey") AdminCustomerSortKey sortKey,
            @Param("sortOrder") SortOrder sortOrder, @Param("offset") int offset, @Param("limit") int limit);

    @Query(value = """
            select count(*)
            from users u
            where (:name is null or :name = '' or u.name like concat('%', :name, '%'))
            """, nativeQuery = true)
    long countUsers(@Param("name") String name);

    @Query(value = """
            select
                    *
                from
            """, nativeQuery = true)
    long countUsers();
}
