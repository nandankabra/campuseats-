package com.example.campuseatsOrders.dto;

import com.example.campuseatsOrders.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long userRef,
        Long foodServiceRef,
        BigDecimal totalAmount,
        String deliveryAddress,
        OrderStatus status,
        List<OrderItemResponse> items
) {
}