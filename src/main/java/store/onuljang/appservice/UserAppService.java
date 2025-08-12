package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.entity.Users;
import store.onuljang.service.UserService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAppService {
    UserService userService;

    @Transactional(readOnly = true)
    public boolean existName(String name) {
        return !userService.findByName(name);
    }

    @Transactional
    public void modifyName(String uid, String name) {
        Users user = userService.findByUId(uid);
        user.modifyName(name);
    }
}
