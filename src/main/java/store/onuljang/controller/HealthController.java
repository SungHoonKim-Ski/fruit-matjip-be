package store.onuljang.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/health")
@RestController
public class HealthController {

    @GetMapping
    public ResponseEntity<Void> getHealth() {
        return ResponseEntity.ok().build();
    }
}
