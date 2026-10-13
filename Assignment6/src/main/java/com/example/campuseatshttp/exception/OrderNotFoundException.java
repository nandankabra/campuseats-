package com.example.campuseatshttp.exception;

public class OrderNotFoundException extends ApiException {
    public OrderNotFoundException(Long id) {
        super("order-not-found", "Order Not Found", 404, "Order with id " + id + " was not found");
    }
}
