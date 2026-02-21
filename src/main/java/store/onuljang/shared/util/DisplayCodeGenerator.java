package store.onuljang.shared.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class DisplayCodeGenerator {

    private static final char[] SAFE_CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();
    private static final int SAFE_CHARS_LENGTH = SAFE_CHARS.length;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_RETRY = 3;

    private static final Pattern CODE_PATTERN = Pattern.compile("^[RD]-\\d{8}-[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{5,6}$");
    private static final Pattern BACKFILL_PATTERN = Pattern.compile("^[RD]-\\d+$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");

    private DisplayCodeGenerator() {
    }

    public static String generate(String prefix, LocalDateTime dateTime) {
        String dateTimePart = String.format("%02d%02d%02d%02d",
                dateTime.getYear() % 100, dateTime.getMonthValue(), dateTime.getHour(), dateTime.getMinute());
        String randomPart = randomString(5);
        return prefix + "-" + dateTimePart + "-" + randomPart;
    }

    public static String generateFallback(String prefix, LocalDateTime dateTime) {
        String dateTimePart = String.format("%02d%02d%02d%02d",
                dateTime.getYear() % 100, dateTime.getMonthValue(), dateTime.getHour(), dateTime.getMinute());
        String randomPart = randomString(6);
        return prefix + "-" + dateTimePart + "-" + randomPart;
    }

    public static String generateUnique(String prefix, LocalDateTime dateTime, Predicate<String> existsCheck) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            String code = (attempt < MAX_RETRY - 1)
                    ? generate(prefix, dateTime)
                    : generateFallback(prefix, dateTime);
            if (!existsCheck.test(code)) {
                return code;
            }
        }
        throw new IllegalStateException("고유 표시 코드 생성에 실패했습니다.");
    }

    public static String resolveCode(String prefix, String identifier) {
        if (identifier.startsWith(prefix + "-")) {
            validateFormat(identifier);
            return identifier;
        }
        if (NUMERIC_PATTERN.matcher(identifier).matches()) {
            return prefix + "-" + identifier;
        }
        throw new IllegalArgumentException("유효하지 않은 코드 형식입니다: " + identifier);
    }

    private static void validateFormat(String code) {
        if (!CODE_PATTERN.matcher(code).matches() && !BACKFILL_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException("유효하지 않은 코드 형식입니다: " + code);
        }
    }

    private static String randomString(int length) {
        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            result[i] = SAFE_CHARS[RANDOM.nextInt(SAFE_CHARS_LENGTH)];
        }
        return new String(result);
    }
}
