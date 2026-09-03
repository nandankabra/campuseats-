package com.example.campuseatsOrders.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long foodItemRef,
        String itemName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}