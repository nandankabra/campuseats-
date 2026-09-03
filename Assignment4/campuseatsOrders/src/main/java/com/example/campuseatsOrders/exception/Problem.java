package com.example.campuseatsOrders.exception;

public record Problem(
        String type,
        String title,
        int status,
        String detail
) {
}
