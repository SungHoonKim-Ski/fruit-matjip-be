package store.onuljang.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@UtilityClass
public class CursorUtil {
    public record Cursor(Long id, BigDecimal sortValue) {
    }

    public Cursor decode(String cursor) {
        if (cursor == null || cursor.isBlank())
            return new Cursor(null, null);

        String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        String[] parts = decoded.split("\\|");
        Long id = Long.valueOf(parts[0]);
        BigDecimal value = new BigDecimal(parts[1]);
        return new Cursor(id, value);
    }

    public String encode(Long id, BigDecimal sortValue) {
        if (id == null || sortValue == null)
            return null;
        String raw = id + "|" + sortValue.toPlainString();
        return Base64.getUrlEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
