package store.onuljang.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import store.onuljang.shared.entity.enums.UserPointTransactionType;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.user.dto.PointTransactionResponse;
import store.onuljang.shared.user.entity.UserPointTransaction;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.user.repository.UserRepository;
import store.onuljang.shared.user.service.UserPointService;
import store.onuljang.support.IntegrationTestBase;

@DisplayName("포인트 서비스 통합 테스트")
class UserPointServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserPointService userPointService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private Users user;

    @BeforeEach
    void setUp() {
        user = testFixture.createUser("포인트테스트");
    }

    @Nested
    @DisplayName("포인트 적립")
    class EarnTests {

        @Test
        @DisplayName("정상 적립 시 잔액이 증가한다")
        void 정상_적립() {
            // Arrange
            BigDecimal amount = new BigDecimal("5000");

            // Act
            UserPointTransaction tx = userPointService.earn(
                    user.getUid(), amount, UserPointTransactionType.EARN_ADMIN,
                    "테스트 적립", null, null, "admin");
            entityManager.flush();
            entityManager.clear();

            // Assert
            assertThat(tx.getAmount()).isEqualByComparingTo(amount);
            assertThat(tx.getBalanceAfter()).isEqualByComparingTo(amount);

            Users reloaded = userRepository.findByUid(user.getUid()).orElseThrow();
            assertThat(reloaded.getPointBalance()).isEqualByComparingTo(amount);
        }

        @Test
        @DisplayName("0 이하 금액 적립 시 예외가 발생한다")
        void 금액_0이하_예외() {
            // Arrange
            BigDecimal zeroAmount = BigDecimal.ZERO;
            BigDecimal negativeAmount = new BigDecimal("-1000");

            // Act & Assert
            assertThatThrownBy(() -> userPointService.earn(
                    user.getUid(), zeroAmount, UserPointTransactionType.EARN_ADMIN,
                    "0원 적립", null, null, "admin"))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("0보다 커야 합니다");

            assertThatThrownBy(() -> userPointService.earn(
                    user.getUid(), negativeAmount, UserPointTransactionType.EARN_ADMIN,
                    "음수 적립", null, null, "admin"))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("0보다 커야 합니다");
        }

        @Test
        @DisplayName("연속 적립 시 잔액이 누적된다")
        void 연속_적립_누적() {
            // Arrange
            BigDecimal first = new BigDecimal("3000");
            BigDecimal second = new BigDecimal("2000");

            // Act
            userPointService.earn(user.getUid(), first, UserPointTransactionType.EARN_ADMIN,
                    "첫 번째 적립", null, null, "admin");
            UserPointTransaction tx2 = userPointService.earn(
                    user.getUid(), second, UserPointTransactionType.EARN_CS,
                    "두 번째 적립", null, null, "cs");
            entityManager.flush();
            entityManager.clear();

            // Assert
            assertThat(tx2.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("5000"));

            Users reloaded = userRepository.findByUid(user.getUid()).orElseThrow();
            assertThat(reloaded.getPointBalance()).isEqualByComparingTo(new BigDecimal("5000"));
        }

        @Test
        @DisplayName("referenceType과 referenceId가 거래 내역에 저장된다")
        void 참조정보_저장() {
            // Arrange & Act
            UserPointTransaction tx = userPointService.earn(
                    user.getUid(), new BigDecimal("1000"), UserPointTransactionType.EARN_CS,
                    "CS 적립", "COURIER_CLAIM", 42L, "admin");

            // Assert
            assertThat(tx.getReferenceType()).isEqualTo("COURIER_CLAIM");
            assertThat(tx.getReferenceId()).isEqualTo(42L);
            assertThat(tx.getCreatedBy()).isEqualTo("admin");
        }
    }

    @Nested
    @DisplayName("포인트 사용")
    class UseTests {

        @Test
        @DisplayName("잔액 내 사용 시 잔액이 감소한다")
        void 정상_사용() {
            // Arrange
            userPointService.earn(user.getUid(), new BigDecimal("10000"),
                    UserPointTransactionType.EARN_ADMIN, "사전 적립", null, null, "admin");

            // Act
            UserPointTransaction tx = userPointService.use(
                    user.getUid(), new BigDecimal("3000"), UserPointTransactionType.USE_COURIER,
                    "쿠폰 사용", null, null, null);
            entityManager.flush();
            entityManager.clear();

            // Assert
            assertThat(tx.getAmount()).isEqualByComparingTo(new BigDecimal("-3000"));
            assertThat(tx.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("7000"));

            Users reloaded = userRepository.findByUid(user.getUid()).orElseThrow();
            assertThat(reloaded.getPointBalance()).isEqualByComparingTo(new BigDecimal("7000"));
        }

        @Test
        @DisplayName("잔액 부족 시 예외가 발생한다")
        void 잔액_부족_예외() {
            // Arrange - 잔액 5000원
            userPointService.earn(user.getUid(), new BigDecimal("5000"),
                    UserPointTransactionType.EARN_ADMIN, "사전 적립", null, null, "admin");

            // Act & Assert
            assertThatThrownBy(() -> userPointService.use(
                    user.getUid(), new BigDecimal("6000"), UserPointTransactionType.USE_COURIER,
                    "잔액 초과 사용", null, null, null))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("포인트 잔액이 부족합니다");
        }

        @Test
        @DisplayName("잔액 전액 사용 시 잔액이 0이 된다")
        void 전액_사용() {
            // Arrange
            userPointService.earn(user.getUid(), new BigDecimal("5000"),
                    UserPointTransactionType.EARN_ADMIN, "사전 적립", null, null, "admin");

            // Act
            UserPointTransaction tx = userPointService.use(
                    user.getUid(), new BigDecimal("5000"), UserPointTransactionType.USE_COURIER,
                    "전액 사용", null, null, null);

            // Assert
            assertThat(tx.getBalanceAfter()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("0 이하 금액 사용 시 예외가 발생한다")
        void 사용_금액_0이하_예외() {
            // Act & Assert
            assertThatThrownBy(() -> userPointService.use(
                    user.getUid(), BigDecimal.ZERO, UserPointTransactionType.USE_COURIER,
                    "0원 사용", null, null, null))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("0보다 커야 합니다");
        }
    }

    @Nested
    @DisplayName("적립 취소")
    class CancelEarnTests {

        @Test
        @DisplayName("적립 취소 시 잔액이 감소한다")
        void 적립_취소() {
            // Arrange
            UserPointTransaction earnTx = userPointService.earn(
                    user.getUid(), new BigDecimal("8000"),
                    UserPointTransactionType.EARN_ADMIN, "적립", null, null, "admin");
            entityManager.flush();
            entityManager.clear();

            // Act
            UserPointTransaction cancelTx = userPointService.cancelEarn(earnTx.getId(), "admin");
            entityManager.flush();
            entityManager.clear();

            // Assert
            assertThat(cancelTx.getType()).isEqualTo(UserPointTransactionType.CANCEL_EARN);
            assertThat(cancelTx.getAmount()).isEqualByComparingTo(new BigDecimal("-8000"));
            assertThat(cancelTx.getBalanceAfter()).isEqualByComparingTo(BigDecimal.ZERO);

            Users reloaded = userRepository.findByUid(user.getUid()).orElseThrow();
            assertThat(reloaded.getPointBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("CS 적립 거래도 취소할 수 있다")
        void CS_적립_취소() {
            // Arrange
            UserPointTransaction earnTx = userPointService.earn(
                    user.getUid(), new BigDecimal("3000"),
                    UserPointTransactionType.EARN_CS, "CS 적립", "COURIER_CLAIM", 1L, "cs");
            entityManager.flush();
            entityManager.clear();

            // Act
            UserPointTransaction cancelTx = userPointService.cancelEarn(earnTx.getId(), "admin");

            // Assert
            assertThat(cancelTx.getType()).isEqualTo(UserPointTransactionType.CANCEL_EARN);
            assertThat(cancelTx.getBalanceAfter()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("사용 거래 취소 시도 시 예외가 발생한다")
        void 사용_거래_적립_취소_예외() {
            // Arrange - 적립 후 사용
            userPointService.earn(user.getUid(), new BigDecimal("5000"),
                    UserPointTransactionType.EARN_ADMIN, "적립", null, null, "admin");
            UserPointTransaction useTx = userPointService.use(
                    user.getUid(), new BigDecimal("2000"), UserPointTransactionType.USE_COURIER,
                    "사용", null, null, null);
            entityManager.flush();
            entityManager.clear();

            // Act & Assert
            assertThatThrownBy(() -> userPointService.cancelEarn(useTx.getId(), "admin"))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("적립 거래만 취소할 수 있습니다");
        }

        @Test
        @DisplayName("존재하지 않는 거래 ID로 취소 시 예외가 발생한다")
        void 존재하지_않는_거래_취소_예외() {
            // Act & Assert
            assertThatThrownBy(() -> userPointService.cancelEarn(-999L, "admin"))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("존재하지 않는 포인트 거래입니다");
        }
    }

    @Nested
    @DisplayName("사용 취소")
    class CancelUseTests {

        @Test
        @DisplayName("사용 취소 시 잔액이 환원된다")
        void 사용_취소() {
            // Arrange
            userPointService.earn(user.getUid(), new BigDecimal("10000"),
                    UserPointTransactionType.EARN_ADMIN, "적립", null, null, "admin");
            UserPointTransaction useTx = userPointService.use(
                    user.getUid(), new BigDecimal("4000"), UserPointTransactionType.USE_COURIER,
                    "사용", null, null, null);
            entityManager.flush();
            entityManager.clear();

            // Act
            UserPointTransaction cancelTx = userPointService.cancelUse(useTx.getId());
            entityManager.flush();
            entityManager.clear();

            // Assert
            assertThat(cancelTx.getType()).isEqualTo(UserPointTransactionType.CANCEL_USE);
            assertThat(cancelTx.getAmount()).isEqualByComparingTo(new BigDecimal("4000"));
            assertThat(cancelTx.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("10000"));

            Users reloaded = userRepository.findByUid(user.getUid()).orElseThrow();
            assertThat(reloaded.getPointBalance()).isEqualByComparingTo(new BigDecimal("10000"));
        }

        @Test
        @DisplayName("USE_STORE 거래도 취소할 수 있다")
        void USE_STORE_취소() {
            // Arrange
            userPointService.earn(user.getUid(), new BigDecimal("5000"),
                    UserPointTransactionType.EARN_ADMIN, "적립", null, null, "admin");
            UserPointTransaction useTx = userPointService.use(
                    user.getUid(), new BigDecimal("1000"), UserPointTransactionType.USE_STORE,
                    "매장 사용", null, null, null);
            entityManager.flush();
            entityManager.clear();

            // Act
            UserPointTransaction cancelTx = userPointService.cancelUse(useTx.getId());

            // Assert
            assertThat(cancelTx.getType()).isEqualTo(UserPointTransactionType.CANCEL_USE);
            assertThat(cancelTx.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("5000"));
        }

        @Test
        @DisplayName("적립 거래 사용 취소 시도 시 예외가 발생한다")
        void 적립_거래_사용_취소_예외() {
            // Arrange
            UserPointTransaction earnTx = userPointService.earn(
                    user.getUid(), new BigDecimal("5000"),
                    UserPointTransactionType.EARN_ADMIN, "적립", null, null, "admin");
            entityManager.flush();
            entityManager.clear();

            // Act & Assert
            assertThatThrownBy(() -> userPointService.cancelUse(earnTx.getId()))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("사용 거래만 취소할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("잔액 조회")
    class BalanceTests {

        @Test
        @DisplayName("초기 잔액은 0이다")
        void 초기_잔액_0() {
            // Act
            BigDecimal balance = userPointService.getBalance(user.getUid());

            // Assert
            assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("적립 후 잔액이 반영된다")
        void 적립_후_잔액_반영() {
            // Arrange
            userPointService.earn(user.getUid(), new BigDecimal("7000"),
                    UserPointTransactionType.EARN_ADMIN, "테스트 적립", null, null, "admin");
            entityManager.flush();
            entityManager.clear();

            // Act
            BigDecimal balance = userPointService.getBalance(user.getUid());

            // Assert
            assertThat(balance).isEqualByComparingTo(new BigDecimal("7000"));
        }
    }

    @Nested
    @DisplayName("일괄 적립")
    class BulkEarnTests {

        @Test
        @DisplayName("전체 사용자 일괄 적립 시 모든 사용자에게 포인트가 지급된다")
        void 전체_일괄_적립() {
            // Arrange
            Users user2 = testFixture.createUser("유저2");
            entityManager.flush();
            entityManager.clear();

            // Act
            int[] result = userPointService.bulkEarn(null, true, new BigDecimal("1000"), "이벤트 지급", "admin");

            // Assert - 최소 2명(setUp user + user2)에게 성공
            assertThat(result[0]).isGreaterThanOrEqualTo(2);
            assertThat(result[1]).isEqualTo(0);

            entityManager.flush();
            entityManager.clear();

            assertThat(userPointService.getBalance(user.getUid()))
                    .isEqualByComparingTo(new BigDecimal("1000"));
            assertThat(userPointService.getBalance(user2.getUid()))
                    .isEqualByComparingTo(new BigDecimal("1000"));
        }

        @Test
        @DisplayName("선택 사용자 일괄 적립 시 해당 사용자에게만 포인트가 지급된다")
        void 선택_일괄_적립() {
            // Arrange
            Users user2 = testFixture.createUser("유저2-벌크");
            Users user3 = testFixture.createUser("유저3-벌크");
            entityManager.flush();
            entityManager.clear();

            // Act - user와 user2에게만 지급
            int[] result = userPointService.bulkEarn(
                    List.of(user.getUid(), user2.getUid()),
                    false,
                    new BigDecimal("2000"),
                    "선택 지급",
                    "admin");

            // Assert
            assertThat(result[0]).isEqualTo(2);
            assertThat(result[1]).isEqualTo(0);

            entityManager.flush();
            entityManager.clear();

            assertThat(userPointService.getBalance(user.getUid()))
                    .isEqualByComparingTo(new BigDecimal("2000"));
            assertThat(userPointService.getBalance(user2.getUid()))
                    .isEqualByComparingTo(new BigDecimal("2000"));
            assertThat(userPointService.getBalance(user3.getUid()))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("0 이하 금액 일괄 적립 시 예외가 발생한다")
        void 일괄_적립_금액_0이하_예외() {
            // Act & Assert
            assertThatThrownBy(() -> userPointService.bulkEarn(
                    null, true, BigDecimal.ZERO, "0원 이벤트", "admin"))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("0보다 커야 합니다");
        }

        @Test
        @DisplayName("대상 없이 선택 일괄 적립 시 예외가 발생한다")
        void 대상_없는_선택_적립_예외() {
            // Act & Assert
            assertThatThrownBy(() -> userPointService.bulkEarn(
                    List.of(), false, new BigDecimal("1000"), "빈 대상", "admin"))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("지급 대상 사용자가 없습니다");
        }
    }

    @Nested
    @DisplayName("포인트 내역 조회")
    class HistoryTests {

        @Test
        @DisplayName("getHistory()가 페이지네이션으로 거래 내역을 createdAt DESC 순으로 반환한다")
        void 페이지네이션_내역_조회() {
            // Arrange
            userPointService.earn(user.getUid(), new BigDecimal("1000"),
                    UserPointTransactionType.EARN_ADMIN, "첫 번째 적립", null, null, "admin");
            userPointService.earn(user.getUid(), new BigDecimal("2000"),
                    UserPointTransactionType.EARN_CS, "두 번째 적립", null, null, "cs");
            userPointService.use(user.getUid(), new BigDecimal("500"),
                    UserPointTransactionType.USE_COURIER, "쿠폰 사용", null, null, null);
            entityManager.flush();
            entityManager.clear();

            // Act
            Page<UserPointTransaction> history = userPointService.getHistory(
                    user.getUid(), PageRequest.of(0, 10));

            // Assert
            assertThat(history.getTotalElements()).isEqualTo(3);
            List<UserPointTransaction> content = history.getContent();
            assertThat(content).hasSize(3);
            // createdAt DESC 순서 확인 (첫 번째가 가장 최근)
            assertThat(content.get(0).getCreatedAt())
                    .isAfterOrEqualTo(content.get(1).getCreatedAt());
            assertThat(content.get(1).getCreatedAt())
                    .isAfterOrEqualTo(content.get(2).getCreatedAt());
        }

        @Test
        @DisplayName("getRecentHistory()가 최근 5건만 반환한다")
        void 최근_5건_조회() {
            // Arrange - 6건 적립
            for (int i = 1; i <= 6; i++) {
                userPointService.earn(user.getUid(), new BigDecimal("1000"),
                        UserPointTransactionType.EARN_ADMIN, i + "번째 적립", null, null, "admin");
            }
            entityManager.flush();
            entityManager.clear();

            // Act
            List<PointTransactionResponse> recentHistory = userPointService.getRecentHistory(user.getUid());

            // Assert
            assertThat(recentHistory).hasSize(5);
        }

        @Test
        @DisplayName("거래 없는 사용자의 history는 빈 결과를 반환한다")
        void 거래_없는_사용자_빈_결과() {
            // Arrange - setUp()에서 생성된 user는 거래 없음

            // Act
            Page<UserPointTransaction> history = userPointService.getHistory(
                    user.getUid(), PageRequest.of(0, 10));
            List<PointTransactionResponse> recentHistory = userPointService.getRecentHistory(user.getUid());

            // Assert
            assertThat(history.getTotalElements()).isEqualTo(0);
            assertThat(history.getContent()).isEmpty();
            assertThat(recentHistory).isEmpty();
        }
    }

    @Nested
    @DisplayName("적립 취소 엣지케이스")
    class CancelEarnEdgeCaseTests {

        @Test
        @DisplayName("적립 후 일부 사용한 뒤 적립 취소 시 잔액 부족이면 예외가 발생한다")
        void 일부_사용_후_적립_취소_잔액_부족_예외() {
            // Arrange - 10000원 적립 후 8000원 사용 → 잔액 2000원
            UserPointTransaction earnTx = userPointService.earn(
                    user.getUid(), new BigDecimal("10000"),
                    UserPointTransactionType.EARN_ADMIN, "적립", null, null, "admin");
            userPointService.use(user.getUid(), new BigDecimal("8000"),
                    UserPointTransactionType.USE_COURIER, "사용", null, null, null);
            entityManager.flush();
            entityManager.clear();

            // Act & Assert - 적립 10000원 취소 시도 → 잔액 2000원으로 부족
            assertThatThrownBy(() -> userPointService.cancelEarn(earnTx.getId(), "admin"))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("포인트 잔액이 부족합니다");
        }

        @Test
        @DisplayName("이미 취소된 거래(CANCEL_EARN)를 재취소 시도하면 예외가 발생한다")
        void 이미_취소된_거래_재취소_예외() {
            // Arrange - 적립 후 취소하여 CANCEL_EARN 거래 생성
            UserPointTransaction earnTx = userPointService.earn(
                    user.getUid(), new BigDecimal("5000"),
                    UserPointTransactionType.EARN_ADMIN, "적립", null, null, "admin");
            entityManager.flush();
            entityManager.clear();

            UserPointTransaction cancelTx = userPointService.cancelEarn(earnTx.getId(), "admin");
            entityManager.flush();
            entityManager.clear();

            // Act & Assert - CANCEL_EARN 거래를 다시 cancelEarn 시도
            assertThatThrownBy(() -> userPointService.cancelEarn(cancelTx.getId(), "admin"))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("적립 거래만 취소할 수 있습니다");
        }
    }
}
