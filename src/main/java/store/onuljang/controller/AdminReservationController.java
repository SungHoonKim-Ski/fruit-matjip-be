package store.onuljang.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    @PostMapping("/{id}")
    public ResponseEntity<Void> togglePicked(@PathVariable Long id, @RequestParam Boolean picked) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports")
    public ResponseEntity<Void> getReports() {
        return ResponseEntity.ok().build();
    }
}


