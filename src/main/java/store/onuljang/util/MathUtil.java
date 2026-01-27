package store.onuljang.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class MathUtil {

    public static double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c;
    }

    public static String trimDistance(double distanceKm) {
        if (Math.floor(distanceKm) == distanceKm) {
            return String.valueOf((int) distanceKm);
        }
        return BigDecimal.valueOf(distanceKm).stripTrailingZeros().toPlainString();
    }

    public static String formatAmount(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }
}
