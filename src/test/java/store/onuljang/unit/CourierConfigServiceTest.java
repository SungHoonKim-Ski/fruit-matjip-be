package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import store.onuljang.courier.entity.CourierConfig;
import store.onuljang.courier.repository.CourierConfigRepository;
import store.onuljang.courier.service.CourierConfigService;
import store.onuljang.shared.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class CourierConfigServiceTest {

    @InjectMocks private CourierConfigService courierConfigService;

    @Mock private CourierConfigRepository courierConfigRepository;

    private CourierConfig buildConfig(Long id) {
        CourierConfig config =
                CourierConfig.builder()
                        .islandSurcharge(new BigDecimal("3000"))
                        .noticeText("안내 문구")
                        .senderName("홍길동")
                        .senderPhone("010-1234-5678")
                        .senderPhone2(null)
                        .senderAddress("서울시 강남구")
                        .senderDetailAddress("101호")
                        .build();
        ReflectionTestUtils.setField(config, "id", id);
        return config;
    }

    @Nested
    @DisplayName("getConfig - 택배 설정 조회")
    class GetConfig {

        @Test
        @DisplayName("설정이 존재하면 반환")
        void getConfig_exists_returnsConfig() {
            // arrange
            CourierConfig config = buildConfig(1L);
            given(courierConfigRepository.findById(1L)).willReturn(Optional.of(config));

            // act
            CourierConfig result = courierConfigService.getConfig();

            // assert
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getIslandSurcharge()).isEqualByComparingTo(new BigDecimal("3000"));
            assertThat(result.getSenderName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("설정이 없으면 NotFoundException")
        void getConfig_notFound_throwsException() {
            // arrange
            given(courierConfigRepository.findById(1L)).willReturn(Optional.empty());

            // act / assert
            assertThatThrownBy(() -> courierConfigService.getConfig())
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("택배 설정이 존재하지 않습니다");
        }
    }

    @Nested
    @DisplayName("updateConfig - 택배 설정 수정")
    class UpdateConfig {

        @Test
        @DisplayName("설정을 저장하고 저장된 엔티티를 반환")
        void updateConfig_savesAndReturns() {
            // arrange
            CourierConfig config = buildConfig(1L);
            config.update(
                    true,
                    new BigDecimal("5000"),
                    new BigDecimal("3000"),
                    false,
                    1,
                    "변경된 안내",
                    "김철수",
                    "010-9999-8888",
                    null,
                    "부산시 해운대구",
                    "202호");
            given(courierConfigRepository.save(config)).willReturn(config);

            // act
            CourierConfig result = courierConfigService.updateConfig(config);

            // assert
            then(courierConfigRepository).should().save(config);
            assertThat(result.isEnabled()).isTrue();
            assertThat(result.getIslandSurcharge()).isEqualByComparingTo(new BigDecimal("5000"));
            assertThat(result.getSenderName()).isEqualTo("김철수");
        }
    }
}
