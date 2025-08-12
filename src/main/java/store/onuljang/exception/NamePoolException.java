package store.onuljang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class NamePoolException extends CustomRuntimeException {
    public NamePoolException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
