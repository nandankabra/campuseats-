package com.example.campuseatsOrders.model;

import com.example.campuseatsOrders.dto.OrderItemResponse;
import com.example.campuseatsOrders.dto.OrderResponse;

import java.math.BigDecimal;
import java.util.List;

public class Order {

    private final Long orderId;
    private final Long userRef;
    private final Long foodServiceRef;
    private final BigDecimal totalAmount;
    private final String deliveryAddress;
    private final String idempotencyKey;

    private OrderStatus status;
    private final List<OrderItem> items;

    public Order(
            Long orderId,
            Long userRef,
            Long foodServiceRef,
            BigDecimal totalAmount,
            String deliveryAddress,
            OrderStatus status,
            List<OrderItem> items,
            String idempotencyKey) {

        this.orderId = orderId;
        this.userRef = userRef;
        this.foodServiceRef = foodServiceRef;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.items = items;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserRef() {
        return userRef;
    }

    public Long getFoodServiceRef() {
        return foodServiceRef;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    /*
     * Internal record -> public REST representation.
     *
     * idempotencyKey is deliberately NOT exposed.
     */
    public OrderResponse asJson() {

        List<OrderItemResponse> itemResponses =
                items.stream()
                        .map(item ->
                                new OrderItemResponse(
                                        item.getFoodItemRef(),
                                        item.getItemName(),
                                        item.getUnitPrice(),
                                        item.getQuantity(),
                                        item.getLineTotal()
                                ))
                        .toList();

        return new OrderResponse(
                orderId,
                userRef,
                foodServiceRef,
                totalAmount,
                deliveryAddress,
                status,
                itemResponses
        );
    }
}