package com.learn.notifiy.aop;

import com.learn.notifiy.error.AppError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.management.JMException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // DTO validation exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    // handle login exceptions
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AppError> handleAuthExceptions(AuthenticationException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid Credentials", errors);
    }

    // handle jwt filter exceptions
    @ExceptionHandler({
            JMException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<AppError> handleJwtExceptions(Exception ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid, or Expired Token", null);
    }


    // custom business exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AppError> handleBusinessLogicExceptions(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    private ResponseEntity<AppError> buildResponse(HttpStatus status, String message, Map<String, String> errors) {
        return ResponseEntity
                .status(status)
                .body(
                        new AppError(
                                status.value(),
                                message,
                                errors,
                                System.currentTimeMillis()
                        ));
    }
}
