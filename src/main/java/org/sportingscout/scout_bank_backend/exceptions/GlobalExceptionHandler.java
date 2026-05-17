package org.sportingscout.scout_bank_backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import jakarta.validation.ConstraintViolationException;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
    Map<String, String> fieldErrors = new HashMap<>();

    ex.getConstraintViolations().forEach(violation -> {
      String propertyPath = violation.getPropertyPath().toString();
      String message = violation.getMessage();
      fieldErrors.put(propertyPath, message);
    });

    Map<String, Object> errorBody = new HashMap<>();
    errorBody.put("timestamp", Instant.now().toString());
    errorBody.put("status", HttpStatus.BAD_REQUEST.value());
    errorBody.put("error", "Bad Request");
    errorBody.put("message", "Database constraint validation failed");
    errorBody.put("errors", fieldErrors);

    return ResponseEntity.badRequest().body(errorBody);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();

    // Loop through only the fields that failed validation
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      fieldErrors.put(fieldName, errorMessage);
    });

    Map<String, Object> errorBody = new HashMap<>();
    errorBody.put("timestamp", Instant.now().toString());
    errorBody.put("status", HttpStatus.BAD_REQUEST.value());
    errorBody.put("error", "Bad Request");
    errorBody.put("message", "Validation failed for your request payload");
    errorBody.put("errors", fieldErrors); // Contains cleanly formatted field errors
    errorBody.put("path", ex.getBody().getDetail()); // Safe metadata path info

    return ResponseEntity.badRequest().body(errorBody);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

    Map<String, Object> errorBody = new HashMap<>();
    errorBody.put("timestamp", Instant.now().toString());
    errorBody.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
    errorBody.put("error", "Method Not Allowed");

    String message = String.format("HTTP method '%s' is not supported for this endpoint. Supported methods are: %s",
        ex.getMethod(), ex.getSupportedHttpMethods());
    errorBody.put("message", message);
    errorBody.put("path", request.getRequestURI());

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorBody);
  }
}
