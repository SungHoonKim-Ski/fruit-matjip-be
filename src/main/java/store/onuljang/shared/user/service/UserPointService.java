package store.onuljang.shared.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shared.entity.enums.UserPointTransactionType;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.user.dto.PointTransactionResponse;
import store.onuljang.shared.user.entity.UserPointTransaction;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.user.repository.UserPointTransactionRepository;

import java.math.BigDecimal;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import store.onuljang.shared.user.repository.UserRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPointService {

    UserService userService;
    UserRepository userRepository;
    UserPointTransactionRepository userPointTransactionRepository;

    public BigDecimal getBalance(String uid) {
        Users user = userService.findByUId(uid);
        return user.getPointBalance();
    }

    @Transactional
    public UserPointTransaction earn(
        String uid,
        BigDecimal amount,
        UserPointTransactionType type,
        String description,
        String referenceType,
        Long referenceId,
        String createdBy
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new UserValidateException("포인트 금액은 0보다 커야 합니다.");
        }
        Users user = userService.findByUidWithLock(uid);
        user.addPointBalance(amount);
        UserPointTransaction tx = UserPointTransaction.builder()
            .user(user)
            .type(type)
            .amount(amount)
            .balanceAfter(user.getPointBalance())
            .description(description)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .createdBy(createdBy)
            .build();
        return userPointTransactionRepository.save(tx);
    }

    @Transactional
    public UserPointTransaction use(
        String uid,
        BigDecimal amount,
        UserPointTransactionType type,
        String description,
        String referenceType,
        Long referenceId
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new UserValidateException("포인트 금액은 0보다 커야 합니다.");
        }
        Users user = userService.findByUidWithLock(uid);
        user.subtractPointBalance(amount);
        UserPointTransaction tx = UserPointTransaction.builder()
            .user(user)
            .type(type)
            .amount(amount.negate())
            .balanceAfter(user.getPointBalance())
            .description(description)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .build();
        return userPointTransactionRepository.save(tx);
    }

    @Transactional
    public UserPointTransaction cancelEarn(Long transactionId, String adminId) {
        UserPointTransaction original = userPointTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new UserValidateException("존재하지 않는 포인트 거래입니다."));
        UserPointTransactionType originalType = original.getType();
        if (originalType != UserPointTransactionType.EARN_CS
            && originalType != UserPointTransactionType.EARN_ADMIN) {
            throw new UserValidateException("적립 거래만 취소할 수 있습니다.");
        }
        Users user = userService.findByUidWithLock(original.getUser().getUid());
        user.subtractPointBalance(original.getAmount());
        UserPointTransaction cancelTx = UserPointTransaction.builder()
            .user(user)
            .type(UserPointTransactionType.CANCEL_EARN)
            .amount(original.getAmount().negate())
            .balanceAfter(user.getPointBalance())
            .description("적립 취소: " + original.getDescription())
            .referenceType("POINT_TX")
            .referenceId(transactionId)
            .createdBy(adminId)
            .build();
        return userPointTransactionRepository.save(cancelTx);
    }

    @Transactional
    public UserPointTransaction cancelUse(Long transactionId) {
        UserPointTransaction original = userPointTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new UserValidateException("존재하지 않는 포인트 거래입니다."));
        UserPointTransactionType originalType = original.getType();
        if (originalType != UserPointTransactionType.USE_COURIER
            && originalType != UserPointTransactionType.USE_STORE) {
            throw new UserValidateException("사용 거래만 취소할 수 있습니다.");
        }
        Users user = userService.findByUidWithLock(original.getUser().getUid());
        BigDecimal usedAmount = original.getAmount().abs();
        user.addPointBalance(usedAmount);
        UserPointTransaction cancelTx = UserPointTransaction.builder()
            .user(user)
            .type(UserPointTransactionType.CANCEL_USE)
            .amount(usedAmount)
            .balanceAfter(user.getPointBalance())
            .description("사용 취소: " + original.getDescription())
            .referenceType("POINT_TX")
            .referenceId(transactionId)
            .build();
        return userPointTransactionRepository.save(cancelTx);
    }

    public Page<UserPointTransaction> getHistory(String uid, Pageable pageable) {
        Users user = userService.findByUId(uid);
        return userPointTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public List<PointTransactionResponse> getRecentHistory(String uid) {
        Users user = userService.findByUId(uid);
        return userPointTransactionRepository.findTop5ByUserOrderByCreatedAtDesc(user)
            .stream()
            .map(PointTransactionResponse::from)
            .toList();
    }

    @Transactional
    public int[] bulkEarn(
            List<String> uids,
            boolean allUsers,
            BigDecimal amount,
            String description,
            String createdBy) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new UserValidateException("포인트 금액은 0보다 커야 합니다.");
        }
        List<Users> targets;
        if (allUsers) {
            targets = userRepository.findAll();
        } else {
            if (uids == null || uids.isEmpty()) {
                throw new UserValidateException("지급 대상 사용자가 없습니다.");
            }
            targets = uids.stream()
                    .map(userService::findByUId)
                    .toList();
        }
        int success = 0;
        int fail = 0;
        for (Users user : targets) {
            try {
                user.addPointBalance(amount);
                UserPointTransaction tx = UserPointTransaction.builder()
                        .user(user)
                        .type(UserPointTransactionType.EARN_ADMIN)
                        .amount(amount)
                        .balanceAfter(user.getPointBalance())
                        .description(description)
                        .createdBy(createdBy)
                        .build();
                userPointTransactionRepository.save(tx);
                success++;
            } catch (Exception e) {
                log.warn("포인트 일괄 지급 실패: uid={}, error={}", user.getUid(), e.getMessage());
                fail++;
            }
        }
        return new int[] {success, fail};
    }
}
