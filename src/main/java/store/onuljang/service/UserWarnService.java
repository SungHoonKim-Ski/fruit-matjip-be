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
