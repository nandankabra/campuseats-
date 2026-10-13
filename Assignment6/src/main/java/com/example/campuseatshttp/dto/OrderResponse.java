package com.example.campuseatshttp.dto;

import com.example.campuseatshttp.model.OrderItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "order")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

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
    private String status;

    public OrderResponse() {
    }

    public OrderResponse(Long orderId, String customerName, String address, List<OrderItem> items, BigDecimal totalAmount, String status) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.address = address;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderResponse that = (OrderResponse) o;
        return Objects.equals(orderId, that.orderId) &&
                Objects.equals(customerName, that.customerName) &&
                Objects.equals(address, that.address) &&
                Objects.equals(totalAmount, that.totalAmount) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, customerName, address, totalAmount, status);
    }
}