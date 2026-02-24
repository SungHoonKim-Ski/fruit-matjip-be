package store.onuljang.shared.user.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.onuljang.shared.entity.enums.UserPointTransactionType;
import store.onuljang.shared.user.dto.*;
import store.onuljang.shared.user.entity.UserPointTransaction;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.user.service.UserPointService;
import store.onuljang.shared.user.service.UserService;

@RestController
@RequestMapping("/api/admin/points")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AdminPointController {

    UserPointService userPointService;
    UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminPointUserResponse>> searchUsers(
        @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        List<Users> users = userService.getUsers(keyword, null, null, null, null, 20);
        return ResponseEntity.ok(users.stream().map(AdminPointUserResponse::from).toList());
    }

    @GetMapping("/users/{uid}/history")
    public ResponseEntity<PointHistoryResponse> getUserHistory(
        @PathVariable String uid,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
            PointHistoryResponse.from(userPointService.getHistory(uid, PageRequest.of(page, size)))
        );
    }

    @PostMapping("/issue")
    public ResponseEntity<PointTransactionResponse> issuePoints(
        @Valid @RequestBody AdminPointIssueRequest request
    ) {
        UserPointTransaction tx = userPointService.earn(
            request.uid(),
            request.amount(),
            UserPointTransactionType.EARN_ADMIN,
            request.description(),
            null,
            null,
            "admin"
        );
        return ResponseEntity.ok(PointTransactionResponse.from(tx));
    }

    @PostMapping("/deduct")
    public ResponseEntity<PointTransactionResponse> deductPoints(
        @Valid @RequestBody AdminPointDeductRequest request
    ) {
        UserPointTransaction tx = userPointService.use(
            request.uid(),
            request.amount(),
            UserPointTransactionType.USE_STORE,
            request.description(),
            null,
            null
        );
        return ResponseEntity.ok(PointTransactionResponse.from(tx));
    }

    @PostMapping("/bulk-issue")
    public ResponseEntity<AdminPointBulkIssueResponse> bulkIssuePoints(
        @Valid @RequestBody AdminPointBulkIssueRequest request
    ) {
        int[] result = userPointService.bulkEarn(
            request.uids(),
            request.allUsers(),
            request.amount(),
            request.description(),
            "admin"
        );
        java.math.BigDecimal totalAmount = request.amount()
            .multiply(java.math.BigDecimal.valueOf(result[0]));
        return ResponseEntity.ok(
            new AdminPointBulkIssueResponse(result[0], result[1], totalAmount)
        );
    }
}
