package store.onuljang.shop.admin.util;

public record Pagination(
        boolean hasNext,
        String nextCursor
) {}
