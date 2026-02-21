package store.onuljang.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class KakaoPayException extends CustomRuntimeException {
    public KakaoPayException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
