package store.onuljang.courier.dto;

import java.util.List;

public record TrackingUploadErrorResponse(String message, List<TrackingUploadError> errors) {}
