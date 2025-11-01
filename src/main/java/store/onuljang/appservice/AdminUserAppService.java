package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.AdminUpdateReservationsRequest;
import store.onuljang.controller.response.AdminReservationListResponse;
import store.onuljang.controller.response.AdminReservationsTodayResponse;
import store.onuljang.exception.UserValidateException;
import store.onuljang.log.user_message.UserMessageEvent;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.ReservationSalesRow;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.MessageType;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.service.*;
import store.onuljang.util.TimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class AdminUserAppService {
    UserService userService;
    UserWarnService userWarnService;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    public void warn(UUID uid) {
        Users user = userService.findByUidWithLock(uid.toString());

        user.warn();
        userWarnService.warnByAdmin(user);

        publishUserNoShowMessage(uid);
    }

    private void publishUserNoShowMessage(UUID uid) {
        eventPublisher.publishEvent(UserMessageEvent.builder()
            .userUid(uid.toString())
            .type(MessageType.USER_NO_SHOW)
            .build());
    }
}
