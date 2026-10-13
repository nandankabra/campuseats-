package com.example.campuseatshttp.exception;

public class PaymentDeclinedException extends ApiException {
    public PaymentDeclinedException(String reason) {
        super("payment-declined", "Payment Declined", 422, reason);
    }
}
