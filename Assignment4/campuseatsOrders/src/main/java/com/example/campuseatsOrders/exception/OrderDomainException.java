package com.example.campuseatsOrders.exception;

public class OrderDomainException extends RuntimeException {

    public OrderDomainException(String message) {
        super(message);
    }
}
