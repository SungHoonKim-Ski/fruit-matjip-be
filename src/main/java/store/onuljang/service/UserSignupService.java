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
public class UserSignupService {
    UserRepository userRepository;
    NameGenerator nameGenerator;

    @Transactional
    public Users ensureUserBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId)
            .orElseGet(() -> {
                String uniqueName = nameGenerator.generate();
                Users newUser = Users.builder()
                    .name(uniqueName)
                    .socialId(socialId)
                    .uuid(UUID.randomUUID())
                    .build();

                return userRepository.save(newUser);
            });
    }
}
