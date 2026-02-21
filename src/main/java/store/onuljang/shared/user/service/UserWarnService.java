package store.onuljang.shared.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shared.user.repository.UserWarnRepository;
import store.onuljang.shared.user.entity.UserWarn;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.entity.enums.UserWarnReason;

import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class UserWarnService {
    UserWarnRepository userWarnRepository;

    @Transactional(readOnly = true)
    public List<UserWarn> findAllByUser(Users user) {
        return userWarnRepository.findAllByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void noShow(Users user) {
        userWarnRepository.save(new UserWarn(user, UserWarnReason.NO_SHOW));
    }

    @Transactional
    public void noShows(Users user, long times) {
        ArrayList<UserWarn> warnArrayList = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            warnArrayList.add(new UserWarn(user, UserWarnReason.NO_SHOW));
        }
        userWarnRepository.saveAll(warnArrayList);
    }

    @Transactional
    public void warnByAdmin(Users user) {
        userWarnRepository.save(new UserWarn(user, UserWarnReason.ADMIN));
    }
}
