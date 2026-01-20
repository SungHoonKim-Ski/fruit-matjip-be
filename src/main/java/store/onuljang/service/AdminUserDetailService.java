package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.AdminRepository;
import store.onuljang.repository.entity.Admin;
import store.onuljang.service.dto.AdminUserDetails;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminUserDetailService implements UserDetailsService {
    AdminRepository adminRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));

        return new AdminUserDetails(admin);
    }
}
