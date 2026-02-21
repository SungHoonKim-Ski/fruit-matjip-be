package store.onuljang.shared.user.exception;

import store.onuljang.shared.exception.CustomRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class UserNotFoundException extends CustomRuntimeException {
    public UserNotFoundException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
