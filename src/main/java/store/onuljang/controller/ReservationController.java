package store.onuljang.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    @PostMapping("/")
    public ResponseEntity<?> create() {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<Void> cancel() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public ResponseEntity<?> get() {
        return ResponseEntity.ok().build();
    }
}


