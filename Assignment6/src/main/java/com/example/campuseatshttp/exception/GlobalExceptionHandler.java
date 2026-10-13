package com.example.campuseatshttp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final MediaType PROBLEM_JSON_UTF8 =
            MediaType.parseMediaType("application/problem+json; charset=utf-8");

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Problem> handleApiException(ApiException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatus());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        Problem problem = new Problem(
                ex.getType(),
                ex.getTitle(),
                ex.getStatus(),
                ex.getMessage()
        );

        return ResponseEntity.status(status)
                .contentType(PROBLEM_JSON_UTF8)
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorDetail> errorList = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errorList.add(new FieldErrorDetail(fe.getField(), fe.getDefaultMessage()));
        }

        Problem problem = new Problem(
                "validation-error",
                "Validation Failed",
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Request validation failed with " + errorList.size() + " error(s)",
                errorList
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(PROBLEM_JSON_UTF8)
                .body(problem);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<String> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/plain; charset=utf-8"));
        return new ResponseEntity<>("406 Not Acceptable: Supported representations are application/json and application/xml\n", headers, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Problem> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        Problem problem = new Problem(
                "method-not-allowed",
                "Method Not Allowed",
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(PROBLEM_JSON_UTF8)
                .body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleGenericException(Exception ex) {
        // Log full stack trace and internal message safely server-side
        log.error("Unhandled internal exception caught: {}", ex.getMessage(), ex);

        // Sanitize error response: never leak stack traces or internal DB/system details
        Problem problem = new Problem(
                "internal-error",
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected internal error occurred. Please contact system support."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(PROBLEM_JSON_UTF8)
                .body(problem);
    }
}