package store.onuljang.shared.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shared.user.repository.UserDeliveryInfoRepository;
import store.onuljang.shared.user.entity.UserDeliveryInfo;
import store.onuljang.shared.user.entity.Users;

import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
public class UserDeliveryInfoService {
    UserDeliveryInfoRepository userDeliveryInfoRepository;

    public Optional<UserDeliveryInfo> findByUser(Users user) {
        return userDeliveryInfoRepository.findByUser(user);
    }

    @Transactional
    public UserDeliveryInfo create(Users user, String phone, String postalCode, String address1, String address2,
            Double latitude, Double longitude) {
        Double lat = latitude == null ? 0.0 : latitude;
        Double lng = longitude == null ? 0.0 : longitude;
        UserDeliveryInfo created = UserDeliveryInfo.builder()
            .user(user)
            .phone(phone)
            .postalCode(postalCode)
            .address1(address1)
            .address2(address2)
            .latitude(lat)
            .longitude(lng)
            .build();
        return userDeliveryInfoRepository.save(created);
    }

    @Transactional
    public UserDeliveryInfo update(UserDeliveryInfo info, String phone, String postalCode, String address1,
            String address2, Double latitude, Double longitude) {
        Double lat = latitude == null ? 0.0 : latitude;
        Double lng = longitude == null ? 0.0 : longitude;
        info.update(phone, postalCode, address1, address2, lat, lng);
        return info;
    }
}
