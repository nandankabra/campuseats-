package com.example.campuseatsOrders.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemRequest(

        @NotNull(message = "foodItemRef is required")
        @Positive(message = "foodItemRef must be positive")
        Long foodItemRef,

        @NotBlank(message = "itemName is required")
        String itemName,

        @NotNull(message = "unitPrice is required")
        @DecimalMin(value = "0.00", message = "unitPrice cannot be negative")
        BigDecimal unitPrice,

        @Positive(message = "quantity must be greater than zero")
        int quantity

) {
}