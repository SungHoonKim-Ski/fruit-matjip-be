package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.response.UserMessageResponse;
import store.onuljang.exception.ExistUserNameException;
import store.onuljang.exception.UserNoContentException;
import store.onuljang.repository.entity.UserMessageQueue;
import store.onuljang.repository.entity.Users;
import store.onuljang.service.UserMessageQueueService;
import store.onuljang.service.UserNameLogService;
import store.onuljang.service.UserService;
import store.onuljang.util.TimeUtil;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAppService {
    UserService userService;
    UserNameLogService userNameLogService;
    UserMessageQueueService userMessageQueueService;

    @Transactional
    public void modifyName(String uid, String name) {
        if (!existName(name)) {
            throw new ExistUserNameException("이미 존재하는 닉네임입니다.");
        }
        Users user = userService.findByUId(uid);

        userNameLogService.save(user.getUid(), user.getName(), name);

        user.modifyName(name);
    }

    @Transactional
    public void messageReceived(long messageId) {
        UserMessageQueue message = userMessageQueueService.findById(messageId);

        message.markReceived(TimeUtil.nowDateTime());
    }

    @Transactional(readOnly = true)
    public boolean existName(String name) {
        return !userService.existUserByName(name);
    }

    @Transactional(readOnly = true)
    public boolean canSelfPick(String uid) {
        Users user = userService.findByUId(uid);

        return !user.exceedMaxWarnCount();
    }

    @Transactional(readOnly = true)
    public UserMessageResponse getMessage(String uid) {
        UserMessageQueue message = userMessageQueueService.findFirstPendingWithMessageTemplate(uid)
                .orElseThrow(() -> new UserNoContentException("유저 메시지가 없습니다."));

        message.markSent(TimeUtil.nowDateTime());

        return UserMessageResponse.from(message);
    }
}
