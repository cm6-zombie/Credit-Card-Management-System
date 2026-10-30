
package com.creditcard.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        errors.putIfAbsent(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                );

        ApiErrorResponse response = new ApiErrorResponse(
                400,
                "Bad Request",
                "Validation failed",
                errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException exception) {

        int status = exception.getStatusCode().value();

        HttpStatus httpStatus = HttpStatus.resolve(status);

        String error = httpStatus != null
                ? httpStatus.getReasonPhrase()
                : "Request Error";

        ApiErrorResponse response = new ApiErrorResponse(
                status,
                error,
                exception.getReason() != null
                        ? exception.getReason()
                        : "Request failed",
                Map.of()
        );

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(response);
    }
}
