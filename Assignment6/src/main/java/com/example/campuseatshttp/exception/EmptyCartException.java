package com.example.campuseatshttp.exception;

public class EmptyCartException extends ApiException {
    public EmptyCartException(String detail) {
        super("empty-cart", "Empty Cart", 422, detail);
    }
}
