package store.onuljang.controller.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminProductCategoriesRequest(@NotNull List<Long> categoryIds) {
}
