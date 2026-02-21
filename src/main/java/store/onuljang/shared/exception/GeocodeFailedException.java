package store.onuljang.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class GeocodeFailedException extends CustomRuntimeException {
    public GeocodeFailedException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
