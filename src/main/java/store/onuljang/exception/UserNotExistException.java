package store.onuljang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UserNotExistException extends CustomRuntimeException {
    public UserNotExistException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
