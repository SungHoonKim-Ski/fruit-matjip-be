package store.onuljang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ProductExceedException extends CustomRuntimeException {
    public ProductExceedException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
