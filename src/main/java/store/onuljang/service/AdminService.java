package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.exception.NotFoundException;
import store.onuljang.repository.AdminRepository;
import store.onuljang.repository.entity.Admin;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional(readOnly = true)
public class AdminService {
    AdminRepository adminRepository;

    @Transactional(readOnly = true)
    public Admin findById(long id) {
        return adminRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 관리자"));
    }

    @Transactional(readOnly = true)
    public Optional<Admin> existEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    @Transactional
    public Admin save(Admin admin) {
        return adminRepository.save(admin);
    }
}