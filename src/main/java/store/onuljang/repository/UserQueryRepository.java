package store.onuljang.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store.onuljang.controller.request.AdminCustomerScrollRequest;
import store.onuljang.controller.request.AdminCustomerSortKey;
import store.onuljang.controller.request.SortOrder;
import store.onuljang.repository.entity.QUsers;
import store.onuljang.repository.entity.Users;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static store.onuljang.controller.request.AdminCustomerSortKey.*;
import static store.onuljang.repository.entity.QUsers.users;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Users> getUsers(
        String name,
        AdminCustomerSortKey sortKey,
        SortOrder sortOrder,
        BigDecimal cursorValue,
        Long cursorId,
        int limit
    ) {
        BooleanBuilder where = new BooleanBuilder();

        if (name != null && !name.isBlank()) {
            where.and(users.name.contains(name));
        }

        // 커서 조건 (첫 페이지면 cursorValue/cursorId 가 null)
        if (cursorValue != null && cursorId != null) {
            switch (sortKey) {
                case TOTAL_REVENUE -> {
                    if (sortOrder == SortOrder.DESC) {
                        where.and(
                            users.totalRevenue.lt(cursorValue)
                                .or(users.totalRevenue.eq(cursorValue).and(users.id.lt(cursorId)))
                        );
                    } else {
                        where.and(
                            users.totalRevenue.gt(cursorValue)
                                .or(users.totalRevenue.eq(cursorValue).and(users.id.gt(cursorId)))
                        );
                    }
                }
                case TOTAL_WARN_COUNT -> {
                    NumberPath<Integer> col = users.totalWarnCount;
                    int cv = cursorValue.intValue();
                    if (sortOrder == SortOrder.DESC) {
                        where.and(
                            col.lt(cv).or(col.eq(cv).and(users.id.lt(cursorId)))
                        );
                    } else {
                        where.and(
                            col.gt(cv).or(col.eq(cv).and(users.id.gt(cursorId)))
                        );
                    }
                }
                case WARN_COUNT -> {
                    NumberPath<Integer> col = users.warnCount;
                    int cv = cursorValue.intValue();
                    if (sortOrder == SortOrder.DESC) {
                        where.and(
                            col.lt(cv).or(col.eq(cv).and(users.id.lt(cursorId)))
                        );
                    } else {
                        where.and(
                            col.gt(cv).or(col.eq(cv).and(users.id.gt(cursorId)))
                        );
                    }
                }
            }
        }

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        switch (sortKey) {
            case TOTAL_REVENUE ->
                orders.add(sortOrder == SortOrder.DESC ? users.totalRevenue.desc() : users.totalRevenue.asc());
            case TOTAL_WARN_COUNT ->
                orders.add(sortOrder == SortOrder.DESC ? users.totalWarnCount.desc() : users.totalWarnCount.asc());
            case WARN_COUNT ->
                orders.add(sortOrder == SortOrder.DESC ? users.warnCount.desc() : users.warnCount.asc());
        }
        orders.add(sortOrder == SortOrder.DESC ? users.id.desc() : users.id.asc());

        return queryFactory
            .selectFrom(users)
            .where(where)
            .orderBy(orders.toArray(OrderSpecifier[]::new))
            .limit(limit + 1L) // hasNext 판단용
            .fetch();
    }
}
