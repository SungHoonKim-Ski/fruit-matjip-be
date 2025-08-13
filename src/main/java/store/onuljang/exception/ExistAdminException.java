package store.onuljang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ExistAdminException extends CustomRuntimeException {
    public ExistAdminException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
