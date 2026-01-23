package store.onuljang.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Getter
public class DeliveryConfigDto {
    @Value("${DELIVERY.STORE_LAT}")
    double storeLat;

    @Value("${DELIVERY.STORE_LNG}")
    double storeLng;

    @Value("${DELIVERY.MAX_DISTANCE_KM:3}")
    double maxDistanceKm;

    @Value("${DELIVERY.FEE_DISTANCE_KM:1.5}")
    double feeDistanceKm;

    @Value("${DELIVERY.MIN_AMOUNT:15000}")
    BigDecimal minAmount;

    @Value("${DELIVERY.FEE_NEAR:2900}")
    BigDecimal feeNear;

    @Value("${DELIVERY.FEE_PER_100M:50}")
    BigDecimal feePer100m;

    @Value("${DELIVERY.START_HOUR:12}")
    int startHour;

    @Value("${DELIVERY.START_MINUTE:0}")
    int startMinute;

    @Value("${DELIVERY.END_HOUR:19}")
    int endHour;

    @Value("${DELIVERY.END_MINUTE:30}")
    int endMinute;

}
