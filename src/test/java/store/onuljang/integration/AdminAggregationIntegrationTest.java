package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.onuljang.controller.response.AdminReservationSummaryResponse;
import store.onuljang.controller.response.AdminReservationDetailsResponse;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.support.IntegrationTestBase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static store.onuljang.util.TimeUtil.nowDate;

/**
 * 관리자 집계 API 통합 테스트
 *
 * API Spec: - GET /api/admin/agg/summary?from={date}&to={date} - 집계 요약 조회 - GET
 * /api/admin/agg/sales?date={date} - 일별 판매 상세 조회
 */
class AdminAggregationIntegrationTest extends IntegrationTestBase {

    private Admin admin;
    private Users user1;
    private Users user2;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        user1 = testFixture.createUser("유저1");
        user2 = testFixture.createUser("유저2");
        setAdminAuthentication(admin);
    }

    @Nested
    @DisplayName("GET /api/admin/agg/summary - 집계 요약 조회")
    class GetAggregationSummary {

        @Test
        @DisplayName("날짜 범위로 집계 요약 조회 성공")
        void getAggregationSummary_Success() throws Exception {
            // given
            // API가 과거/현재 날짜만 허용하므로 yesterday 사용
            LocalDate yesterday = nowDate().minusDays(1);
            Product product = testFixture.createProduct("상품", 10, new BigDecimal("10000"), yesterday, admin);
            testFixture.createReservationWithStatus(user1, product, 2, ReservationStatus.PICKED);
            testFixture.createReservationWithStatus(user2, product, 3, ReservationStatus.PICKED);

            String fromDate = yesterday.format(DateTimeFormatter.ISO_DATE);
            String toDate = yesterday.format(DateTimeFormatter.ISO_DATE);

            // when
            var response = getAction("/api/admin/agg/summary?from=" + fromDate + "&to=" + toDate,
                    AdminReservationSummaryResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body()).isNotNull();
        }

        @Test
        @DisplayName("예약이 없는 기간 조회 시 빈 결과")
        void getAggregationSummary_EmptyPeriod() throws Exception {
            // given
            LocalDate pastDate = nowDate().minusDays(30);
            String fromDate = pastDate.format(DateTimeFormatter.ISO_DATE);
            String toDate = pastDate.plusDays(1).format(DateTimeFormatter.ISO_DATE);

            // when
            var response = getAction("/api/admin/agg/summary?from=" + fromDate + "&to=" + toDate,
                    AdminReservationSummaryResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/admin/agg/sales - 일별 판매 상세 조회")
    class GetDailySales {

        @Test
        @DisplayName("특정 날짜 판매 상세 조회 성공")
        void getDailySales_Success() throws Exception {
            // given
            LocalDate yesterday = nowDate().minusDays(1);
            Product product1 = testFixture.createProduct("상품1", 10, new BigDecimal("10000"), yesterday, admin);
            Product product2 = testFixture.createProduct("상품2", 10, new BigDecimal("20000"), yesterday, admin);
            testFixture.createReservationWithStatus(user1, product1, 2, ReservationStatus.PICKED);
            testFixture.createReservationWithStatus(user2, product2, 1, ReservationStatus.PICKED);

            String date = yesterday.format(DateTimeFormatter.ISO_DATE);

            // when
            var response = getAction("/api/admin/agg/sales?date=" + date, AdminReservationDetailsResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body()).isNotNull();
        }

        @Test
        @DisplayName("판매가 없는 날짜 조회")
        void getDailySales_NoSales() throws Exception {
            // given
            LocalDate pastDate = nowDate().minusDays(30);
            String date = pastDate.format(DateTimeFormatter.ISO_DATE);

            // when
            var response = getAction("/api/admin/agg/sales?date=" + date, AdminReservationDetailsResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
        }
    }
}
