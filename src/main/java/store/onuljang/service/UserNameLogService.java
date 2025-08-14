package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store.onuljang.repository.UserNameLogRepository;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.log.UserNameLog;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class UserNameLogService {
    UserNameLogRepository userNameLogRepository;

    public void save(Users user, String before, String after) {
        userNameLogRepository.save(new UserNameLog(user, before, after));
    }
}
