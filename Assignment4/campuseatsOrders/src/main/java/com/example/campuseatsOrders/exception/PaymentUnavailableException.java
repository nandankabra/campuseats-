package com.example.campuseatsOrders.exception;

public class PaymentUnavailableException extends RuntimeException {

    public PaymentUnavailableException(String message) {
        super(message);
    }
}