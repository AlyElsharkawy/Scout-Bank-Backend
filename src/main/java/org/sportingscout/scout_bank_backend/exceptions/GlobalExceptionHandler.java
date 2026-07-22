package org.sportingscout.scout_bank_backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.beans.factory.annotation.Value;

import jakarta.validation.ConstraintViolationException;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @Value("${spring.servlet.multipart.max-file-size:10MB}")
  private String maxFileSize;

  // Handles Service/Business Level Validation Errors
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
  }

  // Handles Missing Entities (HTTP 404)
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(
      NoSuchElementException ex, HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
  }

  // Handles JPA / RequestParam validation errors
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {

    Map<String, String> fieldErrors = new HashMap<>();
    ex.getConstraintViolations()
        .forEach(violation -> fieldErrors.put(violation.getPropertyPath().toString(), violation.getMessage()));

    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Database constraint validation failed", request.getRequestURI(),
        fieldErrors);
  }

  // Handles DTO @Valid body validation errors
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      fieldErrors.put(fieldName, errorMessage);
    });

    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed for request payload", request.getRequestURI(),
        fieldErrors);
  }

  // Handles Wrong HTTP Methods (e.g. GET instead of POST)
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

    String message = String.format("HTTP method '%s' is not supported for this endpoint. Supported: %s",
        ex.getMethod(), ex.getSupportedHttpMethods());

    return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, message, request.getRequestURI(), null);
  }

  // Handles missing query/form params like missing 'fileName'
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, Object>> handleMissingParams(
      MissingServletRequestParameterException ex, HttpServletRequest request) {

    String message = "Required request parameter '" + ex.getParameterName() + "' is missing";
    return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
  }

  // Global Catch-All for standardizing internal server errors (HTTP 500)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(
      Exception ex, HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred",
        request.getRequestURI());
  }

  // Unsupported message content ereor
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

    return buildErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "Content-Type " + ex.getContentType() + "is not supported. Supported media types are: "
            + ex.getSupportedMediaTypes(),
        request.getRequestURI());
  }

  // Content too large error
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Map<String, Object>> handleMaxSizeException(MaxUploadSizeExceededException exc,
      HttpServletRequest request) {

    Map<String, String> response = new HashMap<>();
    response.put("error", "File exceeds maximum allowed size of " + maxFileSize);

    return buildErrorResponse(HttpStatus.CONTENT_TOO_LARGE, "File exceeds maximum size of " + maxFileSize,
        request.getRequestURI());
  }

  private ResponseEntity<Map<String, Object>> buildErrorResponse(
      HttpStatus status, String message, String path, Map<String, String> fieldErrors) {

    Map<String, Object> errorBody = new HashMap<>();
    errorBody.put("timestamp", Instant.now().toString());
    errorBody.put("status", status.value());
    errorBody.put("error", status.getReasonPhrase());
    errorBody.put("message", message);
    errorBody.put("path", path);

    if (fieldErrors != null && !fieldErrors.isEmpty()) {
      errorBody.put("errors", fieldErrors);
    }

    return ResponseEntity.status(status).body(errorBody);
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<Map<String, Object>> handleMissingServletRequestPartException(
      MissingServletRequestPartException ex,
      HttpServletRequest request) {

    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        String.format("Required multipart request part '%s' is not present", ex.getRequestPartName()),
        request.getRequestURI(), null);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpServletRequest request) {

    String detailMessage = "Required request body is missing or unreadable";

    if (ex.getCause() != null) {
      detailMessage = "Malformed JSON request or invalid data format";
    }

    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        detailMessage,
        request.getRequestURI());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNoResourceFound(
      NoResourceFoundException ex,
      HttpServletRequest request) {

    return buildErrorResponse(
        HttpStatus.NOT_FOUND, // 404 Not Found
        "The requested endpoint or resource was not found: " + request.getRequestURI(),
        request.getRequestURI());
  }

  private ResponseEntity<Map<String, Object>> buildErrorResponse(
      HttpStatus status, String message, String path) {
    return buildErrorResponse(status, message, path, null);
  }

}
