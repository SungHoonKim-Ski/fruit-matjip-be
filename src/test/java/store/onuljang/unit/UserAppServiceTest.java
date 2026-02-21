package store.onuljang.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.onuljang.shared.user.appservice.UserAppService;
import store.onuljang.shared.user.exception.ExistUserNameException;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.user.service.UserMessageQueueService;
import store.onuljang.shared.user.service.UserNameLogService;
import store.onuljang.shared.user.service.UserService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * UserAppService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class UserAppServiceTest {

    @InjectMocks
    private UserAppService userAppService;

    @Mock
    private UserService userService;

    @Mock
    private UserNameLogService userNameLogService;

    @Mock
    private UserMessageQueueService userMessageQueueService;

    private Users testUser;

    @BeforeEach
    void setUp() {
        testUser = Users.builder().socialId("social-123").uid(UUID.randomUUID()).name("테스트유저").build();
        testUser.modifyName("테스트유저");
    }

    @Nested
    @DisplayName("modifyName - 닉네임 변경")
    class ModifyName {

        @Test
        @DisplayName("사용 가능한 닉네임으로 변경 성공")
        void modifyName_Success() {
            // given
            String newName = "새닉네임";
            given(userService.existUserByName(newName)).willReturn(false);
            given(userService.findByUId(testUser.getUid())).willReturn(testUser);

            // when
            userAppService.modifyName(testUser.getUid(), newName);

            // then
            assertThat(testUser.getName()).isEqualTo(newName);
            verify(userNameLogService).save(testUser.getUid(), "테스트유저", newName);
        }

        @Test
        @DisplayName("이미 존재하는 닉네임으로 변경 시 예외 발생")
        void modifyName_DuplicateName_ThrowsException() {
            // given
            String duplicateName = "중복닉네임";
            given(userService.existUserByName(duplicateName)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userAppService.modifyName(testUser.getUid(), duplicateName))
                    .isInstanceOf(ExistUserNameException.class).hasMessageContaining("이미 존재하는 닉네임");
        }
    }

    @Nested
    @DisplayName("existName - 닉네임 존재 확인")
    class ExistName {

        @Test
        @DisplayName("사용 가능한 닉네임인 경우 true 반환")
        void existName_Available_ReturnsTrue() {
            // given
            given(userService.existUserByName("새닉네임")).willReturn(false);

            // when
            boolean result = userAppService.existName("새닉네임");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("이미 존재하는 닉네임인 경우 false 반환")
        void existName_AlreadyExists_ReturnsFalse() {
            // given
            given(userService.existUserByName("중복닉네임")).willReturn(true);

            // when
            boolean result = userAppService.existName("중복닉네임");

            // then
            assertThat(result).isFalse();
        }
    }

}
