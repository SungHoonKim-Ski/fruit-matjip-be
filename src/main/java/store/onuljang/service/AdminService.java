package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import store.onuljang.exception.NotFoundException;
import store.onuljang.repository.AdminRepository;
import store.onuljang.repository.entity.Admin;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminService {
    AdminRepository adminRepository;

    public Admin findById(long id) {
        return adminRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 관리자"));
    }
}