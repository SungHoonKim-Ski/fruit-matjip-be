package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.AdminCustomerSortKey;
import store.onuljang.controller.request.SortOrder;
import store.onuljang.exception.NotFoundException;
import store.onuljang.exception.UserNotFoundException;
import store.onuljang.repository.UserQueryRepository;
import store.onuljang.repository.UserRepository;
import store.onuljang.repository.entity.Users;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    UserRepository userRepository;
    UserQueryRepository userQueryRepository;

    @Transactional
    public Users save(Users user) {
        return userRepository.save(user);
    }

    @Transactional
    public Users findByUidWithLock(String uId) {
        return userRepository.findByUidWithLock(uId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 유저"));
    }

    @Transactional(readOnly = true)
    public Optional<Users> findOptionalBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId);
    }

    @Transactional(readOnly = true)
    public Users findBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId)
            .orElseThrow(() -> new UserNotFoundException("유저 검색 서버 에러"));
    }

    @Transactional(readOnly = true)
    public Users findByUId(String uId) {
        return userRepository.findByUid(uId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 유저"));
    }

    @Transactional(readOnly = true)
    public boolean existUserByName(String name) {
        return userRepository.findByName(name).isPresent();
    }

    @Transactional(readOnly = true)
    public List<Users> getUsers(String name, AdminCustomerSortKey sortKey, SortOrder sortOrder, BigDecimal sortValue,
            Long id, int limit) {
        return userQueryRepository.getUsers(name, sortKey, sortOrder, sortValue, id, limit);
    }

    public long countUsers(String name) {
        return userRepository.countUsers(name);
    }
}
