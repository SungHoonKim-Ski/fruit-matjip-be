package store.onuljang.courier.service;

import java.util.List;
import store.onuljang.courier.dto.TrackingUploadError;

public class TrackingUploadException extends RuntimeException {

    private final List<TrackingUploadError> errors;

    public TrackingUploadException(String message, List<TrackingUploadError> errors) {
        super(message);
        this.errors = errors;
    }

    public List<TrackingUploadError> getErrors() {
        return errors;
    }
}
