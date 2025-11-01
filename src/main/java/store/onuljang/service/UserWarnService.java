package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.UserWarnRepository;
import store.onuljang.repository.entity.UserWarn;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.UserWarnReason;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class UserWarnService {
    UserWarnRepository userWarnRepository;

    @Transactional
    public void noShow(Users user) {
        userWarnRepository.save(new UserWarn(user, UserWarnReason.NO_SHOW));
    }

}
