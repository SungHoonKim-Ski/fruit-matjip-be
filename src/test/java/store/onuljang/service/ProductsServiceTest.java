package store.onuljang.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.onuljang.exception.NotFoundException;
import store.onuljang.repository.ProductsRepository;
import store.onuljang.repository.entity.Product;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsServiceTest {

    @Mock
    private ProductsRepository productsRepository;

    @InjectMocks
    private ProductsService productsService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = mock(Product.class);
        when(testProduct.getId()).thenReturn(1L);
    }

    @Test
    @DisplayName("findByIdWithDetailImagesWithLock - 상품 조회 성공 (상세 이미지 포함, Lock)")
    void findByIdWithDetailImagesWithLock_Success() {
        // given
        when(productsRepository.findAllByIdWithLock(1L)).thenReturn(Optional.of(testProduct));

        // when
        Product result = productsService.findByIdWithDetailImagesWithLock(1L);

        // then
        assertThat(result).isEqualTo(testProduct);
        verify(productsRepository).findAllByIdWithLock(1L);
    }

    @Test
    @DisplayName("findByIdWithDetailImagesWithLock - 존재하지 않는 상품 조회 시 예외 발생")
    void findByIdWithDetailImagesWithLock_NotFound_ThrowsException() {
        // given
        when(productsRepository.findAllByIdWithLock(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productsService.findByIdWithDetailImagesWithLock(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 제품");
    }

    @Test
    @DisplayName("save - 상품 저장 성공")
    void save_Success() {
        // given
        when(productsRepository.save(testProduct)).thenReturn(testProduct);

        // when
        long result = productsService.save(testProduct);

        // then
        assertThat(result).isEqualTo(1L);
        verify(productsRepository).save(testProduct);
    }

    @Test
    @DisplayName("findByIdWithLock - 상품 조회 성공 (Lock)")
    void findByIdWithLock_Success() {
        // given
        when(productsRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testProduct));

        // when
        Product result = productsService.findByIdWithLock(1L);

        // then
        assertThat(result).isEqualTo(testProduct);
    }

    @Test
    @DisplayName("findByIdWithLock - 존재하지 않는 상품 조회 시 예외 발생")
    void findByIdWithLock_NotFound_ThrowsException() {
        // given
        when(productsRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productsService.findByIdWithLock(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 제품");
    }

    @Test
    @DisplayName("bulkUpdateSellDateIdIn - 판매일 일괄 업데이트")
    void bulkUpdateSellDateIdIn_Success() {
        // given
        List<Long> ids = List.of(1L, 2L, 3L);
        LocalDate newDate = LocalDate.now();
        when(productsRepository.bulkUpdateSellDateIdIn(ids, newDate)).thenReturn(3);

        // when
        int result = productsService.bulkUpdateSellDateIdIn(ids, newDate);

        // then
        assertThat(result).isEqualTo(3);
    }

    @Test
    @DisplayName("findAllOrderBySellDateDesc - 전체 상품 목록 조회 (판매일 내림차순)")
    void findAllOrderBySellDateDesc_Success() {
        // given
        List<Product> products = List.of(testProduct);
        when(productsRepository.findAllByOrderBySellDateDesc()).thenReturn(products);

        // when
        List<Product> result = productsService.findAllOrderBySellDateDesc();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testProduct);
    }

    @Test
    @DisplayName("findById - 상품 조회 성공")
    void findById_Success() {
        // given
        when(productsRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // when
        Product result = productsService.findById(1L);

        // then
        assertThat(result).isEqualTo(testProduct);
    }

    @Test
    @DisplayName("findById - 존재하지 않는 상품 조회 시 예외 발생")
    void findById_NotFound_ThrowsException() {
        // given
        when(productsRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productsService.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 제품");
    }

    @Test
    @DisplayName("findAllVisibleBetween - 날짜 범위로 공개 상품 조회")
    void findAllVisibleBetween_Success() {
        // given
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(7);
        List<Product> products = List.of(testProduct);
        when(productsRepository.findAllBySellDateBetweenAndVisible(from, to, true)).thenReturn(products);

        // when
        List<Product> result = productsService.findAllVisibleBetween(from, to, true);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByIdWithDetailImages - 상품 조회 성공 (상세 이미지 포함)")
    void findByIdWithDetailImages_Success() {
        // given
        when(productsRepository.findAllById(1L)).thenReturn(Optional.of(testProduct));

        // when
        Product result = productsService.findByIdWithDetailImages(1L);

        // then
        assertThat(result).isEqualTo(testProduct);
    }

    @Test
    @DisplayName("findByIdWithDetailImages - 존재하지 않는 상품 조회 시 예외 발생")
    void findByIdWithDetailImages_NotFound_ThrowsException() {
        // given
        when(productsRepository.findAllById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productsService.findByIdWithDetailImages(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 제품");
    }

    @Test
    @DisplayName("findAllByIdIn - ID 목록으로 상품 조회")
    void findAllByIdIn_Success() {
        // given
        Collection<Long> ids = List.of(1L, 2L, 3L);
        List<Product> products = List.of(testProduct);
        when(productsRepository.findAllByIdIn(ids)).thenReturn(products);

        // when
        List<Product> result = productsService.findAllByIdIn(ids);

        // then
        assertThat(result).hasSize(1);
    }
}
