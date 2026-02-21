package store.onuljang.shared.user.exception;

import store.onuljang.shared.exception.CustomRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ExistUserNameException extends CustomRuntimeException {
    public ExistUserNameException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
