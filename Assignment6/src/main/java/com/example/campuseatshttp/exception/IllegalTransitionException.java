package com.example.campuseatshttp.exception;

public class IllegalTransitionException extends ApiException {
    public IllegalTransitionException(String currentStatus, String targetStatus) {
        super("illegal-transition", "Illegal State Transition", 409,
                "Cannot transition order from status " + currentStatus + " to " + targetStatus);
    }
}
