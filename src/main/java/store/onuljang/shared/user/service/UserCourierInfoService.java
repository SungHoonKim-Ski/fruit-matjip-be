package store.onuljang.shared.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shared.user.repository.UserCourierInfoRepository;
import store.onuljang.shared.user.entity.UserCourierInfo;
import store.onuljang.shared.user.entity.Users;

import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
public class UserCourierInfoService {
    UserCourierInfoRepository userCourierInfoRepository;

    public Optional<UserCourierInfo> findByUser(Users user) {
        return userCourierInfoRepository.findByUser(user);
    }

    @Transactional
    public UserCourierInfo create(Users user, String receiverName, String receiverPhone, String postalCode,
            String address1, String address2) {
        UserCourierInfo created = UserCourierInfo.builder()
            .user(user)
            .receiverName(receiverName)
            .receiverPhone(receiverPhone)
            .postalCode(postalCode)
            .address1(address1)
            .address2(address2)
            .build();
        return userCourierInfoRepository.save(created);
    }

    @Transactional
    public UserCourierInfo update(UserCourierInfo info, String receiverName, String receiverPhone, String postalCode,
            String address1, String address2) {
        info.update(receiverName, receiverPhone, postalCode, address1, address2);
        return info;
    }
}
