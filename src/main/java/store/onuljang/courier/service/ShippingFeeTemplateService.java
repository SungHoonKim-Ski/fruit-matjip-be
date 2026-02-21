package store.onuljang.courier.service;

import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.entity.ShippingFeeTemplate;
import store.onuljang.courier.repository.ShippingFeeTemplateRepository;
import store.onuljang.shared.exception.UserValidateException;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingFeeTemplateService {

    ShippingFeeTemplateRepository shippingFeeTemplateRepository;

    public List<ShippingFeeTemplate> findAll() {
        return shippingFeeTemplateRepository.findAllByOrderBySortOrderAsc();
    }

    public List<ShippingFeeTemplate> findActive() {
        return shippingFeeTemplateRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    public ShippingFeeTemplate findById(Long id) {
        return shippingFeeTemplateRepository
                .findById(id)
                .orElseThrow(() -> new UserValidateException("배송비 템플릿을 찾을 수 없습니다."));
    }

    @Transactional
    public ShippingFeeTemplate save(ShippingFeeTemplate template) {
        return shippingFeeTemplateRepository.save(template);
    }

    @Transactional
    public void delete(Long id) {
        ShippingFeeTemplate template = findById(id);
        shippingFeeTemplateRepository.delete(template);
    }
}
