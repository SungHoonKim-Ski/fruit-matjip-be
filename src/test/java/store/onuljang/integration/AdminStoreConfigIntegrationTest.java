package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.onuljang.shop.admin.dto.AdminStoreConfigRequest;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.reservation.dto.StoreConfigResponse;
import store.onuljang.support.IntegrationTestBase;

import static org.assertj.core.api.Assertions.assertThat;

class AdminStoreConfigIntegrationTest extends IntegrationTestBase {

    private record ErrorResponse(String state, String message) {
    }

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        setAdminAuthentication(admin);
    }

    @Test
    @DisplayName("GET /config - 기본 설정값 조회")
    void getStoreConfig_ReturnsDefaults() throws Exception {
        // when
        var response = getAction("/api/admin/shop/reservations/config", StoreConfigResponse.class);

        // then
        assertThat(response.isOk()).isTrue();
        assertThat(response.body().reservationDeadlineHour()).isEqualTo(19);
        assertThat(response.body().reservationDeadlineMinute()).isEqualTo(30);
        assertThat(response.body().cancellationDeadlineHour()).isEqualTo(19);
        assertThat(response.body().cancellationDeadlineMinute()).isEqualTo(0);
        assertThat(response.body().pickupDeadlineHour()).isEqualTo(20);
        assertThat(response.body().pickupDeadlineMinute()).isEqualTo(0);
    }

    @Test
    @DisplayName("PUT /config - 정상 업데이트")
    void updateStoreConfig_Success() throws Exception {
        // given
        AdminStoreConfigRequest request = new AdminStoreConfigRequest(20, 0, 19, 30, 21, 0);

        // when
        var response = putAction("/api/admin/shop/reservations/config", request, StoreConfigResponse.class);

        // then
        assertThat(response.isOk()).isTrue();
        assertThat(response.body().reservationDeadlineHour()).isEqualTo(20);
        assertThat(response.body().cancellationDeadlineHour()).isEqualTo(19);
        assertThat(response.body().pickupDeadlineHour()).isEqualTo(21);
    }

    @Test
    @DisplayName("PUT /config - cross-midnight 시간 설정 성공")
    void updateStoreConfig_CrossMidnight_Success() throws Exception {
        // given
        AdminStoreConfigRequest request = new AdminStoreConfigRequest(24, 30, 24, 0, 25, 0);

        // when
        var response = putAction("/api/admin/shop/reservations/config", request, StoreConfigResponse.class);

        // then
        assertThat(response.isOk()).isTrue();
        assertThat(response.body().reservationDeadlineHour()).isEqualTo(24);
        assertThat(response.body().pickupDeadlineHour()).isEqualTo(25);
    }

    @Test
    @DisplayName("PUT /config - 순서 역전 시 400 에러")
    void updateStoreConfig_InvalidOrder_Returns400() throws Exception {
        // given - reservation(22:00) > pickup(21:00)
        AdminStoreConfigRequest request = new AdminStoreConfigRequest(22, 0, 19, 0, 21, 0);

        // when
        var response = putAction("/api/admin/shop/reservations/config", request, ErrorResponse.class);

        // then
        assertThat(response.isBadRequest()).isTrue();
    }

    @Test
    @DisplayName("PUT /config → GET /config 반영 확인")
    void updateThenGet_ReturnsUpdatedValues() throws Exception {
        // given
        AdminStoreConfigRequest request = new AdminStoreConfigRequest(25, 0, 24, 0, 26, 0);
        putAction("/api/admin/shop/reservations/config", request, StoreConfigResponse.class);

        // when
        var response = getAction("/api/admin/shop/reservations/config", StoreConfigResponse.class);

        // then
        assertThat(response.isOk()).isTrue();
        assertThat(response.body().reservationDeadlineHour()).isEqualTo(25);
        assertThat(response.body().cancellationDeadlineHour()).isEqualTo(24);
        assertThat(response.body().pickupDeadlineHour()).isEqualTo(26);
    }
}
