package store.onuljang.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import store.onuljang.appservice.ReservationAppService;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.exception.UserValidateException;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.service.ProductsService;
import store.onuljang.service.ReservationService;
import store.onuljang.service.UserService;
import store.onuljang.service.DeliveryOrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ReservationAppService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class ReservationAppServiceTest {

    @InjectMocks
    private ReservationAppService reservationAppService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private UserService userService;

    @Mock
    private ProductsService productsService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DeliveryOrderService deliveryOrderService;

    private Users testUser;
    private Product testProduct;
    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = Admin.builder().name("테스트관리자").email("admin@test.com").password("password").build();
        ReflectionTestUtils.setField(testAdmin, "id", 1L);

        testUser = Users.builder().socialId("social-123").uid(UUID.randomUUID()).name("테스트유저").build();
        testUser.modifyName("테스트유저");
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testProduct = Product.builder().name("테스트상품").stock(10).price(new BigDecimal("10000"))
                .sellDate(LocalDate.now().plusDays(1)).productUrl("https://example.com/image.jpg").visible(true)
                .selfPick(true).registeredAdmin(testAdmin).build();
        ReflectionTestUtils.setField(testProduct, "id", 1L);
    }

    @Nested
    @DisplayName("reserve - 예약 생성")
    class Reserve {

        @Test
        @DisplayName("예약 생성 성공 시 상품 재고 차감")
        void reserve_DeductsStock() {
            // given
            int initialStock = testProduct.getStock();
            int quantity = 3;
            String uid = testUser.getUid();
            ReservationRequest request = new ReservationRequest(1L, quantity, new BigDecimal("30000"));

            given(productsService.findByIdWithLock(1L)).willReturn(testProduct);
            given(userService.findByUidWithLock(uid)).willReturn(testUser);
            given(reservationService.save(any(Reservation.class))).willAnswer(invocation -> {
                Object r = invocation.getArgument(0);
                ReflectionTestUtils.setField(r, "id", Long.valueOf(1L));
                return null;
            });

            // when
            reservationAppService.reserve(uid, request);

            // then
            assertThat(testProduct.getStock()).isEqualTo(initialStock - quantity);
            verify(reservationService).save(any(Reservation.class));
        }

        @Test
        @DisplayName("닉네임 미변경 사용자 예약 시 예외 발생")
        void reserve_NicknameNotChanged_ThrowsException() {
            // given
            Users newUser = Users.builder().socialId("social-new").uid(UUID.randomUUID()).name("신규고객").build();
            ReflectionTestUtils.setField(newUser, "id", 2L);

            ReservationRequest request = new ReservationRequest(1L, 1, new BigDecimal("10000"));
            given(productsService.findByIdWithLock(1L)).willReturn(testProduct);
            given(userService.findByUidWithLock(newUser.getUid())).willReturn(newUser);

            // when & then
            assertThatThrownBy(() -> reservationAppService.reserve(newUser.getUid(), request))
                    .isInstanceOf(UserValidateException.class).hasMessageContaining("닉네임 변경 후");
        }
    }

    @Nested
    @DisplayName("cancel - 예약 취소")
    class Cancel {

        @Test
        @DisplayName("예약 취소 시 재고 복원")
        void cancel_RestoresStock() {
            // given
            int quantity = 3;
            testProduct.reserve(quantity);
            int stockAfterReserve = testProduct.getStock();

            Reservation reservation = Reservation.builder().user(testUser).product(testProduct).quantity(quantity)
                    .amount(new BigDecimal("30000")).sellPrice(new BigDecimal("10000"))
                    .pickupDate(LocalDate.now().plusDays(1)).build();
            ReflectionTestUtils.setField(reservation, "id", 1L);

            given(reservationService.findByIdWithLock(1L)).willReturn(reservation);
            given(productsService.findByIdWithLock(1L)).willReturn(testProduct);
            given(userService.findByUidWithLock(testUser.getUid())).willReturn(testUser);

            // when
            reservationAppService.cancel(testUser.getUid(), 1L);

            // then
            assertThat(testProduct.getStock()).isEqualTo(stockAfterReserve + quantity);
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        @DisplayName("다른 사용자의 예약 취소 시 예외 발생")
        void cancel_NotOwner_ThrowsException() {
            // given
            Users otherUser = Users.builder().socialId("social-other").uid(UUID.randomUUID()).name("다른유저").build();
            otherUser.modifyName("다른유저");
            ReflectionTestUtils.setField(otherUser, "id", 2L);

            Reservation reservation = Reservation.builder().user(otherUser).product(testProduct).quantity(2)
                    .amount(new BigDecimal("20000")).sellPrice(new BigDecimal("10000")).pickupDate(LocalDate.now())
                    .build();
            ReflectionTestUtils.setField(reservation, "id", 1L);

            given(reservationService.findByIdWithLock(1L)).willReturn(reservation);
            given(userService.findByUidWithLock(testUser.getUid())).willReturn(testUser);

            // when & then
            assertThatThrownBy(() -> reservationAppService.cancel(testUser.getUid(), 1L))
                    .isInstanceOf(UserValidateException.class).hasMessageContaining("다른 유저가 예약한 상품");
        }
    }

    @Nested
    @DisplayName("selfPick - 셀프 픽업 요청")
    class SelfPick {

        @Test
        @DisplayName("경고 횟수 초과 사용자 셀프 픽업 요청 시 예외 발생")
        void selfPick_ExceedWarnCount_ThrowsException() {
            // given
            Users warnedUser = Users.builder().socialId("social-warned").uid(UUID.randomUUID()).name("경고유저").build();
            warnedUser.modifyName("경고유저");
            warnedUser.warn(2);
            ReflectionTestUtils.setField(warnedUser, "id", 3L);

            Reservation reservation = Reservation.builder().user(warnedUser).product(testProduct).quantity(2)
                    .amount(new BigDecimal("20000")).sellPrice(new BigDecimal("10000")).pickupDate(LocalDate.now())
                    .build();
            ReflectionTestUtils.setField(reservation, "id", 1L);

            given(reservationService.findByIdWithLock(1L)).willReturn(reservation);
            given(productsService.findById(1L)).willReturn(testProduct);
            given(userService.findByUidWithLock(warnedUser.getUid())).willReturn(warnedUser);

            // when & then
            assertThatThrownBy(() -> reservationAppService.selfPick(warnedUser.getUid(), 1L))
                    .isInstanceOf(UserValidateException.class).hasMessageContaining("셀프 수령 취소 가능 횟수를 초과");
        }
    }

    @Nested
    @DisplayName("getReservations - 예약 목록 조회")
    class GetReservations {

        @Test
        @DisplayName("사용자의 예약 목록 조회 성공")
        void getReservations_Success() {
            // given
            LocalDate from = LocalDate.now();
            LocalDate to = LocalDate.now();

            Reservation reservation = Reservation.builder().user(testUser).product(testProduct).quantity(2)
                    .amount(new BigDecimal("20000")).sellPrice(new BigDecimal("10000")).pickupDate(LocalDate.now())
                    .build();
            ReflectionTestUtils.setField(reservation, "id", 1L);

            given(userService.findByUId(testUser.getUid())).willReturn(testUser);
            given(reservationService.findAllByUserAndPickupDateBetweenWithProductAndDeliveryOrderByPickupDateDesc(testUser, from, to))
                    .willReturn(List.of(reservation));

            // when
            var result = reservationAppService.getReservations(testUser.getUid(), from, to);

            // then
            assertThat(result.response()).hasSize(1);
        }
    }
}
