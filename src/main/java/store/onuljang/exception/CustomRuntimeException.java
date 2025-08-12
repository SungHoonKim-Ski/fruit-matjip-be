package store.onuljang.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class CustomRuntimeException extends RuntimeException {
    private final HttpStatus status;

    public CustomRuntimeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public CustomRuntimeException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}