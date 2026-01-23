package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.UserDeliveryInfoRepository;
import store.onuljang.repository.entity.UserDeliveryInfo;
import store.onuljang.repository.entity.Users;

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
    public UserDeliveryInfo saveOrUpdate(Users user, String phone, String postalCode, String address1, String address2,
            Double latitude, Double longitude) {
        Optional<UserDeliveryInfo> existing = userDeliveryInfoRepository.findByUser(user);
        Double lat = latitude == null ? 0.0 : latitude;
        Double lng = longitude == null ? 0.0 : longitude;
        if (existing.isPresent()) {
            UserDeliveryInfo info = existing.get();
            info.update(phone, postalCode, address1, address2, lat, lng);
            return info;
        }
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
}
