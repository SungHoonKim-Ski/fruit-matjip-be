package store.onuljang.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class AccessTokenParseException extends CustomRuntimeException {
    public AccessTokenParseException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
