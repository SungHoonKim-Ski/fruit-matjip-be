package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.exception.NotFoundException;
import store.onuljang.repository.UserMessageQueueRepository;
import store.onuljang.repository.entity.UserMessageQueue;
import store.onuljang.util.TimeUtil;

import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class UserMessageQueueService {
    UserMessageQueueRepository userMessageQueueRepository;

    @Transactional
    public UserMessageQueue findById(long id) {
        return userMessageQueueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("일치하는 메시지를 찾을 수 없습니다"));
    }

    @Transactional
    public UserMessageQueue save(UserMessageQueue messageQueue) {
        return userMessageQueueRepository.save(messageQueue);
    }

    @Transactional(readOnly = true)
    public Optional<UserMessageQueue> findFirstPendingWithMessageTemplate(String uid) {
        return userMessageQueueRepository.findFirstPendingWithMessageTemplate(uid, TimeUtil.nowDateTime());
    }
}
