package com.example.campuseatshttp.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "order")
public class Order {

    @JacksonXmlProperty(localName = "orderId")
    private Long orderId;

    @JacksonXmlProperty(localName = "customerName")
    private String customerName;

    @JacksonXmlProperty(localName = "address")
    private String address;

    @JacksonXmlElementWrapper(localName = "items")
    @JacksonXmlProperty(localName = "item")
    private List<OrderItem> items = new ArrayList<>();

    @JacksonXmlProperty(localName = "totalAmount")
    private BigDecimal totalAmount;

    @JacksonXmlProperty(localName = "status")
    private OrderStatus status;

    @JacksonXmlProperty(localName = "createdAt")
    private Instant createdAt;

    public Order() {
    }

    public Order(Long orderId,
                 String customerName,
                 String address,
                 List<OrderItem> items,
                 BigDecimal totalAmount,
                 OrderStatus status,
                 Instant createdAt) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.address = address;
        this.items = items;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
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