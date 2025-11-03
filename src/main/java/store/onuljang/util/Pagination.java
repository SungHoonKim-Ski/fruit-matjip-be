package store.onuljang.util;

public record Pagination(
        boolean hasNext,
        String nextCursor
) {}
