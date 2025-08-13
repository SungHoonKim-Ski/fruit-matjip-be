package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store.onuljang.exception.NotFoundException;
import store.onuljang.exception.UserNotFoundException;
import store.onuljang.repository.UserRepository;
import store.onuljang.repository.entity.Users;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class UserService {
    UserRepository userRepository;

    public Users findBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId)
            .orElseThrow(() -> new UserNotFoundException("유저 검색 서버 에러"));
    }

    public Users findByUId(String uId) {
        return userRepository.findByInternalUid(uId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 유저"));
    }

    public boolean findByName(String name) {
        return userRepository.findByName(name).isPresent();
    }
}
