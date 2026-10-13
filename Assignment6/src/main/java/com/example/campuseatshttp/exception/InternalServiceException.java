package com.example.campuseatshttp.exception;

public class InternalServiceException extends ApiException {
    public InternalServiceException(String detail) {
        super("internal-error", "Internal Server Error", 500, detail);
    }
}
