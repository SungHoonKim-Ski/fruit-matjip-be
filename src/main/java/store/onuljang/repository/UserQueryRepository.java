package store.onuljang.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store.onuljang.controller.request.AdminCustomerSortKey;
import store.onuljang.controller.request.SortOrder;
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
        BooleanBuilder where = buildBaseWhere(name);

        applyCursorCondition(where, sortKey, sortOrder, cursorValue, cursorId);

        List<OrderSpecifier<?>> orders = buildOrderSpecifiers(sortKey, sortOrder);

        return queryFactory
            .selectFrom(users)
            .where(where)
            .orderBy(orders.toArray(OrderSpecifier[]::new))
            .limit(limit + 1L) // hasNext 판단용
            .fetch();
    }

    private BooleanBuilder buildBaseWhere(String name) {
        BooleanBuilder where = new BooleanBuilder();
        if (name != null && !name.isBlank()) {
            where.and(users.name.contains(name));
        }
        return where;
    }

    private void applyCursorCondition(
        BooleanBuilder where,
        AdminCustomerSortKey sortKey,
        SortOrder sortOrder,
        BigDecimal cursorValue,
        Long cursorId
    ) {
        // 커서 조건 (첫 페이지면 cursorValue/cursorId 가 null)
        if (cursorValue == null || cursorId == null) {
            return;
        }

        switch (sortKey) {
            case TOTAL_REVENUE -> applyTotalRevenueCursor(where, sortOrder, cursorValue, cursorId);
            case TOTAL_WARN_COUNT -> applyIntegerCursor(where, users.totalWarnCount, sortOrder, cursorValue, cursorId);
            case WARN_COUNT -> applyIntegerCursor(where, users.monthlyWarnCount, sortOrder, cursorValue, cursorId);
        }
    }

    private void applyTotalRevenueCursor(
        BooleanBuilder where,
        SortOrder sortOrder,
        BigDecimal cursorValue,
        Long cursorId
    ) {
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

    private void applyIntegerCursor(
        BooleanBuilder where,
        NumberPath<Integer> column,
        SortOrder sortOrder,
        BigDecimal cursorValue,
        Long cursorId
    ) {
        int cv = cursorValue.intValue();
        if (sortOrder == SortOrder.DESC) {
            where.and(
                column.lt(cv).or(column.eq(cv).and(users.id.lt(cursorId)))
            );
        } else {
            where.and(
                column.gt(cv).or(column.eq(cv).and(users.id.gt(cursorId)))
            );
        }
    }

    private List<OrderSpecifier<?>> buildOrderSpecifiers(
        AdminCustomerSortKey sortKey,
        SortOrder sortOrder
    ) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        switch (sortKey) {
            case TOTAL_REVENUE ->
                orders.add(sortOrder == SortOrder.DESC ? users.totalRevenue.desc() : users.totalRevenue.asc());
            case TOTAL_WARN_COUNT ->
                orders.add(sortOrder == SortOrder.DESC ? users.totalWarnCount.desc() : users.totalWarnCount.asc());
            case WARN_COUNT ->
                orders.add(sortOrder == SortOrder.DESC ? users.monthlyWarnCount.desc() : users.monthlyWarnCount.asc());
        }
        // 항상 id를 tie-breaker 로 사용
        orders.add(sortOrder == SortOrder.DESC ? users.id.desc() : users.id.asc());
        return orders;
    }
}
