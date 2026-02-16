package store.onuljang.auth;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;

    private final ConcurrentHashMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String email) {
        LoginAttempt attempt = attempts.get(email);
        if (attempt == null) return false;

        if (attempt.count >= MAX_ATTEMPTS) {
            if (System.currentTimeMillis() - attempt.lastAttemptMs < LOCK_DURATION_MS) {
                return true;
            }
            attempts.remove(email);
        }
        return false;
    }

    public void loginFailed(String email) {
        attempts.compute(email, (k, v) -> {
            if (v == null) return new LoginAttempt(1, System.currentTimeMillis());
            return new LoginAttempt(v.count + 1, System.currentTimeMillis());
        });
    }

    public void loginSucceeded(String email) {
        attempts.remove(email);
    }

    private record LoginAttempt(int count, long lastAttemptMs) {}
}
