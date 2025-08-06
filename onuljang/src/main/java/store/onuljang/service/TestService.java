package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import store.onuljang.controller.Request.TestRequest;
import store.onuljang.repository.entity.TestEntity;
import store.onuljang.repository.TestRepository;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestService {
    TestRepository testRepository;

    public Long save(TestRequest request) {
        return testRepository.save(
            TestEntity.builder()
                .name(request.name())
                .build()
        ).getId();
    }

    public TestEntity testGet(long id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("test not found"));
    }
}
