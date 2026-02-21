package store.onuljang;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.onuljang.shared.util.TimeUtil;

import java.time.LocalDateTime;

@RequestMapping("/api")
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Void> getHealth() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/time")
    public ResponseEntity<LocalDateTime> getTime() {
        return ResponseEntity.ok(TimeUtil.nowDateTime());
    }
}
