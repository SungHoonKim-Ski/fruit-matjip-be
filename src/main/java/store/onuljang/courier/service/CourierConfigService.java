package store.onuljang.courier.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.entity.CourierConfig;
import store.onuljang.courier.repository.CourierConfigRepository;
import store.onuljang.shared.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierConfigService {

    CourierConfigRepository courierConfigRepository;

    public CourierConfig getConfig() {
        return courierConfigRepository
                .findById(1L)
                .orElseThrow(() -> new NotFoundException("택배 설정이 존재하지 않습니다."));
    }

    @Transactional
    public CourierConfig updateConfig(CourierConfig config) {
        return courierConfigRepository.save(config);
    }
}
