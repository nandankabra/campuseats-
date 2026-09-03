package com.example.campuseatsOrders.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("No order exists with id " + id);
    }
}