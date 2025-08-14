package store.onuljang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ProductExceedException extends CustomRuntimeException {
    public ProductExceedException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
