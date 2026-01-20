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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class NameGenerator {
    NamePoolRepository namePoolRepository;

    @Transactional
    public String generate() {
        List<NamePool> namePools = namePoolRepository.findAll();
        int size = namePools.size();
        if (size == 0) {
            throw new IllegalStateException("이름 생성 서버 에러입니다. 관리자에게 문의하세요.");
        }

        int offset = ThreadLocalRandom.current().nextInt(size);
        NamePool name = namePools.get(offset);

        return generateWithLock(name);
    }

    @Transactional
    public String generateWithLock(NamePool namePool) {
        return namePoolRepository.findByIdWithLock(namePool.getId())
            .orElseThrow(() -> new NamePoolException("이름 생성 서버 에러"))
            .generate();
    }
}
