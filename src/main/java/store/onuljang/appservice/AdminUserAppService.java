package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.AdminCustomerScrollRequest;
import store.onuljang.controller.response.AdminCustomerScrollResponse;
import store.onuljang.controller.response.AdminCustomerWarnResponse;
import store.onuljang.event.user_message.UserMessageEvent;
import store.onuljang.repository.entity.UserWarn;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.MessageType;
import store.onuljang.service.*;
import store.onuljang.util.CursorUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class AdminUserAppService {
    UserService userService;
    UserWarnService userWarnService;
    ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public AdminCustomerScrollResponse getUsers(AdminCustomerScrollRequest request) {
        CursorUtil.Cursor cursor = CursorUtil.decode(request.cursor());

        List<Users> users = userService.getUsers(
                request.name(),
                request.sortKey(),
                request.sortOrder(),
                cursor.sortValue(),
                cursor.id(),
                request.limit()
        );

        boolean hasNext = users.size() > request.limit();
        if (hasNext) {
            Users last = users.get(request.limit() - 1);
            BigDecimal sortValue = switch (request.sortKey()) {
                case TOTAL_REVENUE     -> last.getTotalRevenue();
                case TOTAL_WARN_COUNT  -> BigDecimal.valueOf(last.getTotalWarnCount());
                case WARN_COUNT        -> BigDecimal.valueOf(last.getWarnCount());
            };
            String nextCursor = CursorUtil.encode(last.getId(), sortValue);
            users = users.subList(0, request.limit());
            return AdminCustomerScrollResponse.of(users, true, nextCursor);
        } else {
            return AdminCustomerScrollResponse.of(users, false, null);
        }
    }

    @Transactional(readOnly = true)
    public AdminCustomerWarnResponse getUserWarn(UUID uid) {
        Users user = userService.findByUId(uid.toString());

        List<UserWarn> warn = userWarnService.findAllByUser(user);

        return AdminCustomerWarnResponse.of(warn);
    }

    @Transactional
    public void warn(UUID uid) {
        Users user = userService.findByUidWithLock(uid.toString());

        user.warn();
        userWarnService.warnByAdmin(user);

        publishUserNoShowMessage(uid);
    }

    @Transactional
    public void resetWarn(UUID uid) {
        Users user = userService.findByUidWithLock(uid.toString());

        user.resetWarn();
    }

    private void publishUserNoShowMessage(UUID uid) {
        eventPublisher.publishEvent(UserMessageEvent.builder()
            .userUid(uid.toString())
            .type(MessageType.USER_NO_SHOW)
            .build());
    }
}
