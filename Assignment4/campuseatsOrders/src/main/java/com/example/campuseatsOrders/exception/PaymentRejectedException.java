package com.example.campuseatsOrders.exception;

public class PaymentRejectedException extends RuntimeException {

    public PaymentRejectedException(String message) {
        super(message);
    }
}
