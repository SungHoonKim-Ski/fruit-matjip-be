package store.onuljang.shared.auth.exception;

import store.onuljang.shared.exception.CustomRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class InvalidRefreshTokenException extends CustomRuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
