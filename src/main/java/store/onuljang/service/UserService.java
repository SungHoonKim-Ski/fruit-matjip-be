package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.UserRepository;
import store.onuljang.repository.entity.Users;

import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class UserService {
    UserRepository userRepository;

    public boolean existSocialId(String socialId) {
        return userRepository.findBySocialId(socialId).isPresent();
    }

    public Users findBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new RuntimeException("유저 검색 서버 에러"));
    }

    @Transactional
    public void signUp(String socialId, String name) {
        userRepository.save(
            Users.builder()
                .socialId(socialId)
                .name(name)
                .uuid(UUID.randomUUID())
                .build()
        );
    }
}
