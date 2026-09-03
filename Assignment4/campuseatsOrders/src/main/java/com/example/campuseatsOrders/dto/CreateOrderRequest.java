package com.example.campuseatsOrders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(

        @NotNull(message = "userRef is required")
        @Positive(message = "userRef must be positive")
        Long userRef,

        @NotNull(message = "foodServiceRef is required")
        @Positive(message = "foodServiceRef must be positive")
        Long foodServiceRef,

        @NotNull(message = "totalAmount is required")
        @DecimalMin(value = "0.00", message = "totalAmount cannot be negative")
        BigDecimal totalAmount,

        @NotBlank(message = "deliveryAddress is required")
        String deliveryAddress,

        @NotEmpty(message = "items must contain at least one item")
        List<@Valid OrderItemRequest> items

) {
}