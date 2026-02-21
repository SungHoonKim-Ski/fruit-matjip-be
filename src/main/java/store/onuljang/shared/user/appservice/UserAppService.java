package store.onuljang.shared.user.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shared.user.dto.UserMeResponse;
import store.onuljang.shared.user.dto.UserMessageResponse;
import store.onuljang.shared.user.exception.ExistUserNameException;
import store.onuljang.shared.exception.UserNoContentException;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.user.entity.UserMessageQueue;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.user.service.UserMessageQueueService;
import store.onuljang.shared.user.service.UserNameLogService;
import store.onuljang.shared.user.service.UserService;
import store.onuljang.shared.util.TimeUtil;

@Service
@Transactional(readOnly = true)
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
    public void messageReceived(String uid, long messageId) {
        UserMessageQueue message = userMessageQueueService.findById(messageId);

        if (!message.getUserUid().equals(uid)) {
            throw new UserValidateException("해당 메시지에 대한 권한이 없습니다.");
        }

        message.markReceived(TimeUtil.nowDateTime());
    }

    @Transactional(readOnly = true)
    public boolean existName(String name) {
        return !userService.existUserByName(name);
    }

    @Transactional
    public UserMessageResponse getMessage(String uid) {
        UserMessageQueue message = userMessageQueueService.findFirstPendingByUid(uid)
                .orElseThrow(() -> new UserNoContentException("유저 메시지가 없습니다."));

        message.markSent(TimeUtil.nowDateTime());

        return UserMessageResponse.from(message);
    }

    @Transactional(readOnly = true)
    public UserMeResponse getUserMe(String uid) {
        Users user = userService.findByUId(uid);

        return UserMeResponse.from(user);
    }
}
