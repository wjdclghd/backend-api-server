package com.jch.backendapi.global.error;

import com.jch.backendapi.global.response.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.errorCode();
        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(ErrorResponse.of(errorCode.code(), errorCode.message()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(ErrorResponse.of(errorCode.code(), errorCode.message(), details));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        ErrorCode errorCode = resolveDataIntegrityErrorCode(exception);
        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(ErrorResponse.of(errorCode.code(), errorCode.message()));
    }

    private ErrorCode resolveDataIntegrityErrorCode(DataIntegrityViolationException exception) {
        String message = collectExceptionMessages(exception).toLowerCase();
        if (message.contains("uk_users_email") || message.contains("users_email_key")) {
            return ErrorCode.DUPLICATE_EMAIL;
        }
        return ErrorCode.INTERNAL_SERVER_ERROR;
    }

    private String collectExceptionMessages(Throwable throwable) {
        StringBuilder messages = new StringBuilder();
        Throwable currentThrowable = throwable;
        while (currentThrowable != null) {
            if (currentThrowable.getMessage() != null) {
                messages.append(currentThrowable.getMessage()).append(' ');
            }
            currentThrowable = currentThrowable.getCause();
        }
        return messages.toString();
    }
}
