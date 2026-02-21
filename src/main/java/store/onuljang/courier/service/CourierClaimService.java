package store.onuljang.courier.service;

import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.entity.CourierClaim;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.repository.CourierClaimRepository;
import store.onuljang.shared.entity.enums.CourierClaimStatus;
import store.onuljang.shared.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierClaimService {

    CourierClaimRepository courierClaimRepository;

    @Transactional
    public CourierClaim save(CourierClaim claim) {
        return courierClaimRepository.save(claim);
    }

    public CourierClaim findById(long id) {
        return courierClaimRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 클레임입니다."));
    }

    public List<CourierClaim> findByOrder(CourierOrder order) {
        return courierClaimRepository.findByCourierOrderOrderByIdDesc(order);
    }

    public List<CourierClaim> findAllByStatus(CourierClaimStatus status, int page, int size) {
        return courierClaimRepository.findAllByStatusOrderByIdDesc(
                status, PageRequest.of(page, size));
    }
}
