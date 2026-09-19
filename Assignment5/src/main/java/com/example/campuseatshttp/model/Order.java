package com.example.campuseatshttp.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {

    private Long orderId;
    private String customerName;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Instant createdAt;

    public Order() {
    }

    public Order(Long orderId,
                 String customerName,
                 BigDecimal totalAmount,
                 OrderStatus status,
                 Instant createdAt) {

        this.orderId = orderId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}