package store.onuljang.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.onuljang.shared.user.service.UserService;
import store.onuljang.shop.admin.dto.AdminCustomerSortKey;
import store.onuljang.shop.admin.dto.SortOrder;
import store.onuljang.shared.exception.NotFoundException;
import store.onuljang.shared.user.exception.UserNotFoundException;
import store.onuljang.shared.user.repository.UserQueryRepository;
import store.onuljang.shared.user.repository.UserRepository;
import store.onuljang.shared.user.entity.Users;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserQueryRepository userQueryRepository;

    @InjectMocks
    private UserService userService;

    private Users testUser;

    @BeforeEach
    void setUp() {
        testUser = mock(Users.class);
    }

    @Test
    @DisplayName("save - 사용자 저장 성공")
    void save_Success() {
        // given
        when(userRepository.save(testUser)).thenReturn(testUser);

        // when
        Users result = userService.save(testUser);

        // then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("findByUidWithLock - 사용자 조회 성공 (Lock)")
    void findByUidWithLock_Success() {
        // given
        when(userRepository.findByUidWithLock("test-uid")).thenReturn(Optional.of(testUser));

        // when
        Users result = userService.findByUidWithLock("test-uid");

        // then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByUidWithLock("test-uid");
    }

    @Test
    @DisplayName("findByUidWithLock - 존재하지 않는 사용자 조회 시 예외 발생")
    void findByUidWithLock_NotFound_ThrowsException() {
        // given
        when(userRepository.findByUidWithLock("unknown-uid")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findByUidWithLock("unknown-uid"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 유저");
    }

    @Test
    @DisplayName("findOptionalBySocialId - 소셜 ID로 사용자 조회 성공")
    void findOptionalBySocialId_Success() {
        // given
        when(userRepository.findBySocialId("social-123")).thenReturn(Optional.of(testUser));

        // when
        Optional<Users> result = userService.findOptionalBySocialId("social-123");

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("findOptionalBySocialId - 존재하지 않는 경우 빈 Optional 반환")
    void findOptionalBySocialId_NotFound_ReturnsEmpty() {
        // given
        when(userRepository.findBySocialId("unknown")).thenReturn(Optional.empty());

        // when
        Optional<Users> result = userService.findOptionalBySocialId("unknown");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findBySocialId - 소셜 ID로 사용자 조회 성공")
    void findBySocialId_Success() {
        // given
        when(userRepository.findBySocialId("social-123")).thenReturn(Optional.of(testUser));

        // when
        Users result = userService.findBySocialId("social-123");

        // then
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    @DisplayName("findBySocialId - 존재하지 않는 사용자 조회 시 예외 발생")
    void findBySocialId_NotFound_ThrowsException() {
        // given
        when(userRepository.findBySocialId("unknown")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findBySocialId("unknown"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("유저 검색 서버 에러");
    }

    @Test
    @DisplayName("findByUId - UID로 사용자 조회 성공")
    void findByUId_Success() {
        // given
        when(userRepository.findByUid("test-uid")).thenReturn(Optional.of(testUser));

        // when
        Users result = userService.findByUId("test-uid");

        // then
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    @DisplayName("findByUId - 존재하지 않는 사용자 조회 시 예외 발생")
    void findByUId_NotFound_ThrowsException() {
        // given
        when(userRepository.findByUid("unknown-uid")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findByUId("unknown-uid"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 유저");
    }

    @Test
    @DisplayName("existUserByName - 닉네임 존재하는 경우 true 반환")
    void existUserByName_Exists_ReturnsTrue() {
        // given
        when(userRepository.findByName("중복닉네임")).thenReturn(Optional.of(testUser));

        // when
        boolean result = userService.existUserByName("중복닉네임");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("existUserByName - 닉네임 존재하지 않는 경우 false 반환")
    void existUserByName_NotExists_ReturnsFalse() {
        // given
        when(userRepository.findByName("사용가능닉네임")).thenReturn(Optional.empty());

        // when
        boolean result = userService.existUserByName("사용가능닉네임");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("getUsers - 사용자 목록 조회 성공")
    void getUsers_Success() {
        // given
        List<Users> users = List.of(testUser);
        when(userQueryRepository.getUsers(
                "검색어",
                AdminCustomerSortKey.TOTAL_REVENUE,
                SortOrder.DESC,
                BigDecimal.ZERO,
                null,
                10)).thenReturn(users);

        // when
        List<Users> result = userService.getUsers(
                "검색어",
                AdminCustomerSortKey.TOTAL_REVENUE,
                SortOrder.DESC,
                BigDecimal.ZERO,
                null,
                10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testUser);
    }

    @Test
    @DisplayName("countUsers - 사용자 수 조회")
    void countUsers_Success() {
        // given
        when(userRepository.countUsers("검색어")).thenReturn(42L);

        // when
        long result = userService.countUsers("검색어");

        // then
        assertThat(result).isEqualTo(42L);
    }

    @Test
    @DisplayName("countUsers - 검색어 없을 때 전체 사용자 수 조회")
    void countUsers_WithoutSearchTerm_Success() {
        // given
        when(userRepository.countUsers(null)).thenReturn(100L);

        // when
        long result = userService.countUsers(null);

        // then
        assertThat(result).isEqualTo(100L);
    }
}
