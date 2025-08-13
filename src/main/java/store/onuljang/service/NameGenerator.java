package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.exception.NamePoolException;
import store.onuljang.repository.NamePoolRepository;
import store.onuljang.repository.entity.NamePool;

import java.util.Random;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class NameGenerator {
    NamePoolRepository namePoolRepository;

    @Transactional
    public String generate() {
        int count = (int)getCount();
        int index = new Random().nextInt(count);
        NamePool name = findById(index);
        return name.generate();
    }

    private long getCount() {
        return namePoolRepository.count();
    }

    private NamePool findById(long id) {
        return namePoolRepository.findById(id)
                .orElseThrow(() -> new NamePoolException("이름 생성 서버 에러"));
    }
}
