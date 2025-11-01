package store.onuljang.exception.advice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import store.onuljang.controller.response.ErrorResponse;
import store.onuljang.exception.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .findFirst().orElse("VALIDATION_ERROR");

        log.info("MethodArgumentNotValidException: {}", message);
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(SecurityException ex) {
        log.info("SecurityException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex) {
        log.info("IllegalArgumentException: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex) {
        log.info("AuthenticationException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.info("ConstraintViolationException: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        log.info("IllegalStateException: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        log.info("Missing header: {}", ex.getHeaderName());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("UNAUTHORIZED", "Authorization header is required"));
    }

    @ExceptionHandler({ NoHandlerFoundException.class, NoResourceFoundException.class })
    public ResponseEntity<Void> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // 쿼리 파라미터가 없는 경우 (?from= 누락)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        String msg = "'" + ex.getParameterName() + "' is required";
        return ResponseEntity.badRequest().body(new ErrorResponse("BAD_REQUEST", msg));
    }

    // 타입/포맷이 잘못된 경우 (?from=2025-09-31)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = "Invalid value for '" + ex.getName() + "'";
        return ResponseEntity.badRequest().body(new ErrorResponse("BAD_REQUEST", msg));
    }

    // custom exception
    @ExceptionHandler(AccessTokenParseException.class)
    public ResponseEntity<ErrorResponse> handleTokenParse(AccessTokenParseException ex) {
        log.info("AccessTokenParseException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse("UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(ExistUserNameException.class)
    public ResponseEntity<ErrorResponse> handleExistUserName(ExistUserNameException ex) {
        log.info("ExistUserNameException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse("CONFLICT", ex.getMessage()));
    }


    @ExceptionHandler(ExistAdminException.class)
    public ResponseEntity<ErrorResponse> handleExistAdmin(ExistAdminException ex) {
        log.info("ExistAdminException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        log.info("InvalidRefreshTokenException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse("UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(NamePoolException.class)
    public ResponseEntity<ErrorResponse> handleNamePool(NamePoolException ex) {
        log.info("NamePoolException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse("INTERNAL_SERVER_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        log.info("NotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ProductExceedException.class)
    public ResponseEntity<ErrorResponse> handleProductExceed(ProductExceedException ex) {
        log.info("ProductExceedException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenNotFound(RefreshTokenNotFoundException ex) {
        log.info("RefreshTokenNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse("UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(UserValidateException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenNotFound(UserValidateException ex) {
        log.info("UserValidateException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse("UserValidateException", ex.getMessage()));
    }

    @ExceptionHandler(ProductUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleProductUnavailable(ProductUnavailableException ex) {
        log.info("ProductUnavailableException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse("ProductUnavailableException", ex.getMessage()));
    }


    @ExceptionHandler(UserNoContentException.class)
    public ResponseEntity<ErrorResponse> handleUserNoContentException(UserNoContentException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse("UserNoContentException", ex.getMessage()));
    }

    // Runtime Exception, Exception

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        log.error("RuntimeException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("SERVER_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("SERVER_ERROR", ex.getMessage()));
    }
}


