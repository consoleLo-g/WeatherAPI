package com.example.WeatherAPI.ExcepctionHandeling;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 🟡 400 - Bad Request (validation errors or malformed requests)
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", ex.getMessage(), request.getRequestURI());
    }

    // 🟠 404 - Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), request.getRequestURI());
    }

    // 🔴 500 - Internal Server Error
    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleInternal(InternalServerException ex, HttpServletRequest request) {
        String uri = request.getRequestURI();

        // ✅ Skip Swagger/OpenAPI endpoints completely
        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
            return ResponseEntity.status(HttpStatus.OK).build(); // Let Springdoc handle its own responses
        }

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ex.getMessage(),
                uri);
    }

    // ⚪ Validation errors (Spring validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Validation failed";
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", message, request.getRequestURI());
    }

    // ⚫ Generic fallback - anything not caught above
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();

        // ✅ Explicitly ignore Swagger & OpenAPI routes (never intercept these)
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            // Simply rethrow the exception so Springdoc can respond
            throw new RuntimeException(ex);
        }

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected Error", ex.getMessage(), path);
    }

    // 🧩 Utility method for building consistent JSON responses
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message,
            String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return new ResponseEntity<>(body, status);
    }
}
