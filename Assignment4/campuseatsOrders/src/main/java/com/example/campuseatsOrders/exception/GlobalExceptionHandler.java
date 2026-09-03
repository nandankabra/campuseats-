package com.example.campuseatsOrders.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Problem> handleNotFound(
            OrderNotFoundException ex) {

        Problem problem = new Problem(
                "https://campuseats.example.com/problems/order-not-found",
                "Order not found",
                404,
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problem);
    }

    @ExceptionHandler(OrderConflictException.class)
    public ResponseEntity<Problem> handleConflict(
            OrderConflictException ex) {

        Problem problem = new Problem(
                "https://campuseats.example.com/problems/order-conflict",
                "Order state conflict",
                409,
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problem);
    }

    @ExceptionHandler(OrderDomainException.class)
    public ResponseEntity<Problem> handleDomainError(
            OrderDomainException ex) {

        Problem problem = new Problem(
                "https://campuseats.example.com/problems/domain-refused",
                "Order refused",
                422,
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Problem> handleBadRequest(
            IllegalArgumentException ex) {

        Problem problem = new Problem(
                "https://campuseats.example.com/problems/bad-request",
                "Bad request",
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

        String detail = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error ->
                        error.getField() + ": " +
                                error.getDefaultMessage())
                .orElse("Request validation failed");

        Problem problem = new Problem(
                "https://campuseats.example.com/problems/bad-request",
                "Bad request",
                400,
                detail
        );

        return ResponseEntity
                .badRequest()
                .body(problem);
    }
    @ExceptionHandler(PaymentRejectedException.class)
    public ResponseEntity<Problem> handlePaymentRejected(
            PaymentRejectedException ex) {

        Problem problem = new Problem(
                "https://campuseats.example.com/problems/payment-rejected",
                "Payment rejected",
                422,
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(problem);
    }


    @ExceptionHandler(PaymentUnavailableException.class)
    public ResponseEntity<Problem> handlePaymentUnavailable(
            PaymentUnavailableException ex) {

        Problem problem = new Problem(
                "https://campuseats.example.com/problems/payment-unavailable",
                "Payment service unavailable",
                503,
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(problem);
    }
}