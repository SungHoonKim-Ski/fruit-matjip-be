package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.UserNameLogRepository;
import store.onuljang.repository.entity.log.UserNameLog;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class UserNameLogService {
    UserNameLogRepository userNameLogRepository;

    @Transactional
    public void save(String uId, String before, String after) {
        userNameLogRepository.save(new UserNameLog(uId, before, after));
    }
}
