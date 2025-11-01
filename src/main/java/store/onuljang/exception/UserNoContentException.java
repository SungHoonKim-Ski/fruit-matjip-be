package store.onuljang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NO_CONTENT)
public class UserNoContentException extends CustomRuntimeException {
    public UserNoContentException(String message) {
        super(HttpStatus.NO_CONTENT, message);
    }
}
