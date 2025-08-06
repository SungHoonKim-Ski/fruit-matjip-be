package store.onuljang.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.onuljang.controller.Request.TestRequest;
import store.onuljang.repository.entity.TestEntity;
import store.onuljang.service.TestService;

@RequestMapping("/test")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TestController {
    TestService testService;

    @PostMapping
    public ResponseEntity<Long> saveTestEntity(@RequestBody TestRequest request) {
        return ResponseEntity.ok(testService.save(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestEntity> getTestEntity(@PathVariable Long id) {
        return ResponseEntity.ok(testService.testGet(id));
    }
}
