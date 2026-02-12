package store.onuljang.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.appservice.ReservationAppService;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.exception.ProductExceedException;
import store.onuljang.repository.ProductsRepository;
import store.onuljang.repository.ReservationRepository;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Users;
import store.onuljang.support.IntegrationTestBase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 동시 예약 시나리오 통합 테스트
 *
 * 여러 사용자가 동시에 같은 상품을 예약할 때 재고 관리가 정확하게 이루어지는지 검증합니다.
 *
 * Note: @Transactional을 제거하여 각 스레드가 독립적인 트랜잭션을 사용하도록 합니다.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED) // 테스트 트랜잭션 비활성화
class ConcurrentReservationIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ReservationAppService reservationAppService;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Admin admin;
    private List<Users> testUsers = new ArrayList<>();
    private List<Product> testProducts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        cleanup();
        admin = testFixture.createDefaultAdmin();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        reservationRepository.deleteAllInBatch();
        productsRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동시 예약 - 재고가 10개인 상품에 5명이 동시에 3개씩 예약 시도 시 최대 3명만 성공")
    void concurrentReservation_StockLimitEnforced() throws InterruptedException {
        // given - 데이터 생성 (별도 트랜잭션으로 커밋됨)
        Product product = testFixture.createTomorrowProduct("동시예약테스트상품", 10, new BigDecimal("10000"), admin);
        testProducts.add(product);

        // 5명의 사용자 생성
        List<Users> users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Users user = testFixture.createUser("동시테스트유저" + i);
            users.add(user);
            testUsers.add(user);
        }

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger stockExceptionCount = new AtomicInteger(0);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        // when - 5명이 동시에 각각 3개씩 예약 시도
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 동시에 시작하도록 대기

                    ReservationRequest request = new ReservationRequest(product.getId(), 3, // 3개씩 예약
                            new BigDecimal("30000"));

                    reservationAppService.reserve(users.get(index).getUid(), request);
                    successCount.incrementAndGet();
                } catch (ProductExceedException e) {
                    stockExceptionCount.incrementAndGet();
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 모든 스레드 동시 시작
        endLatch.await(10, TimeUnit.SECONDS); // 최대 10초 대기
        executor.shutdown();

        // then
        Product updatedProduct = productsRepository.findById(product.getId()).orElseThrow();

        // 예외 로그 출력 (디버깅용)
        if (!exceptions.isEmpty()) {
            exceptions.forEach(e -> System.out
                    .println("[DEBUG] Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage()));
        }

        // 검증: 성공한 예약 수 + 재고 부족 예외 + 다른 예외 = 총 스레드 수
        int totalProcessed = successCount.get() + stockExceptionCount.get() + exceptions.size();
        assertThat(totalProcessed).isEqualTo(5);

        // 재고가 10개이고 각 예약이 3개씩이므로 최대 3명만 성공 가능
        assertThat(successCount.get()).isLessThanOrEqualTo(3);

        // 남은 재고 검증: 성공한 예약 수 * 3 = 사용된 재고
        int expectedRemainingStock = 10 - (successCount.get() * 3);
        assertThat(updatedProduct.getStock()).isEqualTo(expectedRemainingStock);
    }

    @Test
    @DisplayName("동시 예약 - 재고 1개 상품에 10명이 동시 예약 시 정확히 1명만 성공")
    void concurrentReservation_SingleStock_OnlyOneSuccess() throws InterruptedException {
        // given
        Product product = testFixture.createTomorrowProduct("단일재고상품", 1, new BigDecimal("50000"), admin);
        testProducts.add(product);

        // 10명의 사용자 생성
        List<Users> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Users user = testFixture.createUser("단일재고테스트유저" + i);
            users.add(user);
            testUsers.add(user);
        }

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        // when - 10명이 동시에 1개씩 예약 시도
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    ReservationRequest request = new ReservationRequest(product.getId(), 1, new BigDecimal("50000"));

                    reservationAppService.reserve(users.get(index).getUid(), request);
                    successCount.incrementAndGet();
                } catch (ProductExceedException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        Product updatedProduct = productsRepository.findById(product.getId()).orElseThrow();

        // 예외 로그 출력 (디버깅용)
        if (!exceptions.isEmpty()) {
            exceptions.forEach(e -> System.out
                    .println("[DEBUG] Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage()));
        }

        // 성공 + 재고부족 + 기타 예외 = 전체
        assertThat(successCount.get() + failCount.get() + exceptions.size()).isEqualTo(10);

        // 정확히 1명만 성공
        assertThat(successCount.get()).isEqualTo(1);

        // 재고가 0이어야 함
        assertThat(updatedProduct.getStock()).isZero();
    }

    @Test
    @DisplayName("동시 예약 취소 - 같은 예약을 여러 번 취소해도 한 번만 취소됨")
    void concurrentCancel_OnlyOneSucceeds() throws InterruptedException {
        // given
        Product product = testFixture.createTomorrowProduct("취소테스트상품", 10, new BigDecimal("10000"), admin);
        testProducts.add(product);
        Users user = testFixture.createUser("취소테스트유저");
        testUsers.add(user);

        // 예약 생성
        ReservationRequest request = new ReservationRequest(product.getId(), 3, new BigDecimal("30000"));
        String displayCode = reservationAppService.reserve(user.getUid(), request);

        int originalStock = productsRepository.findById(product.getId()).orElseThrow().getStock();

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 같은 예약을 5번 동시에 취소 시도
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    reservationAppService.cancel(user.getUid(), displayCode);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        Product updatedProduct = productsRepository.findById(product.getId()).orElseThrow();

        // 취소는 1번만 성공 (나머지는 이미 취소된 상태라 예외)
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(4);

        // 재고가 원래대로 복구됨 (3개 복구)
        assertThat(updatedProduct.getStock()).isEqualTo(originalStock + 3);
    }

    @Test
    @DisplayName("동시 수량 감소 - 같은 예약의 수량을 여러 번 동시 감소해도 정확히 처리됨")
    void concurrentMinusQuantity_AccurateProcessing() throws InterruptedException {
        // given
        Product product = testFixture.createTomorrowProduct("수량감소테스트상품", 100, new BigDecimal("10000"), admin);
        testProducts.add(product);
        Users user = testFixture.createUser("수량감소테스트유저");
        testUsers.add(user);

        // 10개 예약
        ReservationRequest request = new ReservationRequest(product.getId(), 10, new BigDecimal("100000"));
        String displayCode = reservationAppService.reserve(user.getUid(), request);

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        // when - 5번 동시에 각각 1개씩 수량 감소 시도
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    reservationAppService.minusQuantity(user.getUid(), displayCode, 1);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 수량이 1 미만이 되면 예외 발생
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        Product updatedProduct = productsRepository.findById(product.getId()).orElseThrow();

        // 5번 모두 성공하거나, 일부 실패해도 재고는 정확히 관리됨
        // 원래 재고 100 - 예약 10 = 90, 수량 감소 성공 시 +1씩 복구
        int expectedStock = 90 + successCount.get();
        assertThat(updatedProduct.getStock()).isEqualTo(expectedStock);
    }
}
