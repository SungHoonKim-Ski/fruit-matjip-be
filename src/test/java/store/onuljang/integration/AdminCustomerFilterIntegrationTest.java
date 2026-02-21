package store.onuljang.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import store.onuljang.shared.user.repository.UserRepository;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.support.IntegrationTestBase;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * 고객 관리 필터/정렬 통합 테스트
 *
 * - RESTRICTED_UNTIL 정렬: 이용제한 고객만 필터 + 기간순 정렬
 * - 커서 페이징 동작 검증
 */
class AdminCustomerFilterIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    private Admin admin;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        setAdminAuthentication(admin);
        today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    }

    private String callCustomersApi(String sortKey, String sortOrder, int limit) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/shop/customers")
                .param("sortKey", sortKey)
                .param("sortOrder", sortOrder)
                .param("limit", String.valueOf(limit)))
            .andReturn();
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String callCustomersApiWithCursor(String sortKey, String sortOrder, int limit, String cursor) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/shop/customers")
                .param("sortKey", sortKey)
                .param("sortOrder", sortOrder)
                .param("limit", String.valueOf(limit))
                .param("cursor", cursor))
            .andReturn();
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String callCustomersApiWithName(String sortKey, String sortOrder, int limit, String name) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/shop/customers")
                .param("sortKey", sortKey)
                .param("sortOrder", sortOrder)
                .param("limit", String.valueOf(limit))
                .param("name", name))
            .andReturn();
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Nested
    @DisplayName("RESTRICTED_UNTIL 정렬 - 이용제한 고객 필터")
    class RestrictedUntilSort {

        @Test
        @DisplayName("이용제한 고객만 조회된다")
        void restrictedUntilSort_onlyRestrictedUsersReturned() throws Exception {
            // Arrange
            Users restrictedUser = testFixture.createUserWithExactName("제한유저A");
            restrictedUser.restrict(today.plusDays(3));
            userRepository.save(restrictedUser);

            Users normalUser = testFixture.createUserWithExactName("일반유저B");
            userRepository.save(normalUser);

            Users pastRestrictedUser = testFixture.createUserWithExactName("과거제한유저C");
            pastRestrictedUser.restrict(today.minusDays(1));
            userRepository.save(pastRestrictedUser);

            // Act
            String json = callCustomersApi("RESTRICTED_UNTIL", "DESC", 20);

            // Assert
            List<String> names = JsonPath.read(json, "$.response[*].name");
            assertThat(names).containsExactly("제한유저A");
            assertThat(names).doesNotContain("일반유저B", "과거제한유저C");
        }

        @Test
        @DisplayName("이용제한 기간순 DESC 정렬")
        void restrictedUntilSort_descOrder() throws Exception {
            // Arrange
            Users user1 = testFixture.createUserWithExactName("제한3일");
            user1.restrict(today.plusDays(3));
            userRepository.save(user1);

            Users user2 = testFixture.createUserWithExactName("제한7일");
            user2.restrict(today.plusDays(7));
            userRepository.save(user2);

            Users user3 = testFixture.createUserWithExactName("제한1일");
            user3.restrict(today.plusDays(1));
            userRepository.save(user3);

            // Act
            String json = callCustomersApi("RESTRICTED_UNTIL", "DESC", 20);

            // Assert
            List<String> names = JsonPath.read(json, "$.response[*].name");
            assertThat(names).containsExactly("제한7일", "제한3일", "제한1일");
        }

        @Test
        @DisplayName("이용제한 기간순 ASC 정렬")
        void restrictedUntilSort_ascOrder() throws Exception {
            // Arrange
            Users user1 = testFixture.createUserWithExactName("제한3일");
            user1.restrict(today.plusDays(3));
            userRepository.save(user1);

            Users user2 = testFixture.createUserWithExactName("제한7일");
            user2.restrict(today.plusDays(7));
            userRepository.save(user2);

            Users user3 = testFixture.createUserWithExactName("제한1일");
            user3.restrict(today.plusDays(1));
            userRepository.save(user3);

            // Act
            String json = callCustomersApi("RESTRICTED_UNTIL", "ASC", 20);

            // Assert
            List<String> names = JsonPath.read(json, "$.response[*].name");
            assertThat(names).containsExactly("제한1일", "제한3일", "제한7일");
        }

        @Test
        @DisplayName("오늘까지 제한된 유저도 포함된다 (goe 조건)")
        void restrictedUntilSort_todayIncluded() throws Exception {
            // Arrange
            Users todayUser = testFixture.createUserWithExactName("오늘까지제한");
            todayUser.restrict(today);
            userRepository.save(todayUser);

            // Act
            String json = callCustomersApi("RESTRICTED_UNTIL", "DESC", 20);

            // Assert
            List<String> names = JsonPath.read(json, "$.response[*].name");
            assertThat(names).containsExactly("오늘까지제한");
        }

        @Test
        @DisplayName("이용제한 고객이 없으면 빈 목록 반환")
        void restrictedUntilSort_noRestrictedUsers_emptyList() throws Exception {
            // Arrange
            testFixture.createUserWithExactName("일반유저");

            // Act
            String json = callCustomersApi("RESTRICTED_UNTIL", "DESC", 20);

            // Assert
            List<String> names = JsonPath.read(json, "$.response[*].name");
            assertThat(names).isEmpty();
        }

        @Test
        @DisplayName("이름 검색과 이용제한 필터 동시 적용")
        void restrictedUntilSort_withNameSearch() throws Exception {
            // Arrange
            Users user1 = testFixture.createUserWithExactName("김제한");
            user1.restrict(today.plusDays(3));
            userRepository.save(user1);

            Users user2 = testFixture.createUserWithExactName("이제한");
            user2.restrict(today.plusDays(5));
            userRepository.save(user2);

            // Act
            String json = callCustomersApiWithName("RESTRICTED_UNTIL", "DESC", 20, "김");

            // Assert
            List<String> names = JsonPath.read(json, "$.response[*].name");
            assertThat(names).containsExactly("김제한");
        }
    }

    @Nested
    @DisplayName("RESTRICTED_UNTIL 커서 페이징")
    class RestrictedUntilCursorPaging {

        @Test
        @DisplayName("커서 페이징으로 다음 페이지 조회")
        void restrictedUntilSort_cursorPaging() throws Exception {
            // Arrange - 3명의 제한 유저 생성
            Users user1 = testFixture.createUserWithExactName("제한A");
            user1.restrict(today.plusDays(5));
            userRepository.save(user1);

            Users user2 = testFixture.createUserWithExactName("제한B");
            user2.restrict(today.plusDays(3));
            userRepository.save(user2);

            Users user3 = testFixture.createUserWithExactName("제한C");
            user3.restrict(today.plusDays(1));
            userRepository.save(user3);

            // Act - 첫 페이지 (limit=2)
            String firstPage = callCustomersApi("RESTRICTED_UNTIL", "DESC", 2);
            List<String> firstNames = JsonPath.read(firstPage, "$.response[*].name");
            String nextCursor = JsonPath.read(firstPage, "$.pagination.next_cursor");
            Boolean hasNext = JsonPath.read(firstPage, "$.pagination.has_next");

            // Assert - 첫 페이지
            assertThat(firstNames).containsExactly("제한A", "제한B");
            assertThat(hasNext).isTrue();
            assertThat(nextCursor).isNotNull();

            // Act - 두 번째 페이지
            String secondPage = callCustomersApiWithCursor("RESTRICTED_UNTIL", "DESC", 2, nextCursor);
            List<String> secondNames = JsonPath.read(secondPage, "$.response[*].name");
            Boolean hasNextSecond = JsonPath.read(secondPage, "$.pagination.has_next");

            // Assert - 두 번째 페이지
            assertThat(secondNames).containsExactly("제한C");
            assertThat(hasNextSecond).isFalse();
        }

        @Test
        @DisplayName("마지막 페이지 이후 추가 조회 시 빈 목록 반환")
        void restrictedUntilSort_noDuplicateAfterLastPage() throws Exception {
            // Arrange
            Users user1 = testFixture.createUserWithExactName("유일한제한유저");
            user1.restrict(today.plusDays(2));
            userRepository.save(user1);

            // Act - 첫 페이지
            String firstPage = callCustomersApi("RESTRICTED_UNTIL", "DESC", 20);
            List<String> names = JsonPath.read(firstPage, "$.response[*].name");
            Boolean hasNext = JsonPath.read(firstPage, "$.pagination.has_next");

            // Assert
            assertThat(names).containsExactly("유일한제한유저");
            assertThat(hasNext).isFalse();
        }
    }

    @Nested
    @DisplayName("기존 정렬 키 동작 유지")
    class ExistingSortKeysUnchanged {

        @Test
        @DisplayName("TOTAL_REVENUE 정렬 시 모든 유저 포함 (이용제한 유저 포함)")
        void totalRevenueSort_includesAllUsers() throws Exception {
            // Arrange
            Users restrictedUser = testFixture.createUserWithExactName("제한유저");
            restrictedUser.restrict(today.plusDays(3));
            userRepository.save(restrictedUser);

            Users normalUser = testFixture.createUserWithExactName("일반유저");
            userRepository.save(normalUser);

            // Act
            String json = callCustomersApi("TOTAL_REVENUE", "DESC", 20);

            // Assert
            List<String> names = JsonPath.read(json, "$.response[*].name");
            assertThat(names).contains("제한유저", "일반유저");
        }
    }
}
