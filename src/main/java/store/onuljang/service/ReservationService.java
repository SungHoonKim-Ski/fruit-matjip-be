package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.exception.NotFoundException;
import store.onuljang.repository.ProductsRepository;
import store.onuljang.repository.ReservationRepository;
import store.onuljang.repository.UserRepository;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class ReservationService {
    ReservationRepository reservationRepository;
    ProductsRepository productsRepository;
    UserRepository userRepository;

    public Reservation findById(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약"));
    }

    @Transactional
    public long save(String uId, long productId, int quantity, BigDecimal amount) {
        Product product = productsRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 제품"));;

        Users user = userRepository.findByUid(uId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 유저"));

        Reservation entity = Reservation.builder()
            .user(user)
            .product(product)
            .quantity(quantity)
            .amount(amount)
            .orderDate(product.getSellDate())
            .build();

        reservationRepository.save(entity);

        return entity.getId();
    }

    @Transactional
    public void cancel(Reservation entity) {
        entity.changeStatus(ReservationStatus.CANCELED);
    }

    @Transactional
    public List<Reservation> finAllByDateWithUserAndProduct(LocalDate date) {
        return reservationRepository.findAllByOrderDate(date);
    }

    @Transactional
    public List<Reservation> findAllByUserAndOrderDateBetweenWithProduct(Users user, LocalDate from, LocalDate to) {
        return reservationRepository.findAllByUserAndOrderDateBetween(user, from, to);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByStatusAndOrderDateBetween(LocalDate from, LocalDate to) {
        return reservationRepository.findAllByStatusAndOrderDateBetween(ReservationStatus.PICKED ,from, to);
    }
}
