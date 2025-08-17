package store.onuljang.appservice;

import com.amazonaws.services.cloudformation.model.NameAlreadyExistsException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.entity.Users;
import store.onuljang.service.UserNameLogService;
import store.onuljang.service.UserService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAppService {
    UserService userService;
    UserNameLogService userNameLogService;

    @Transactional(readOnly = true)
    public boolean existName(String name) {
        return !userService.existUserByName(name);
    }

    @Transactional
    public void modifyName(String uid, String name) {
        if (!existName(name)) {
            throw new NameAlreadyExistsException("이미 존재하는 닉네임입니다.");
        }
        Users user = userService.findByUId(uid);

        userNameLogService.save(user.getUid(), user.getName(), name);

        user.modifyName(name);
    }
}
