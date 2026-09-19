package com.example.campuseatshttp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Problem> handleRuntime(
            RuntimeException ex) {

        Problem problem = new Problem(
                "Bad Request",
                400,
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidation(
            MethodArgumentNotValidException ex) {

        Problem problem = new Problem(
                "Validation Failed",
                422,
                "Request validation failed"
        );

        return ResponseEntity
                .status(422)
                .body(problem);
    }
}