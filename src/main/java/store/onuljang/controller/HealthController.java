package store.onuljang.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.onuljang.controller.Request.TestRequest;
import store.onuljang.repository.entity.TestEntity;
import store.onuljang.service.TestService;

@RequestMapping("/health")
@RestController
public class HealthController {

    @GetMapping
    public ResponseEntity<Void> getHealth() {
        return ResponseEntity.ok().build();
    }
}
