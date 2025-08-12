package store.onuljang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class RefreshTokenNotFoundException extends CustomRuntimeException {
    public RefreshTokenNotFoundException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
