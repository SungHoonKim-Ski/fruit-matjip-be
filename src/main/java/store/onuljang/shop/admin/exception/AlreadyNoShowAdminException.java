package store.onuljang.shop.admin.exception;

import store.onuljang.shared.exception.CustomRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class AlreadyNoShowAdminException extends CustomRuntimeException {
    public AlreadyNoShowAdminException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
