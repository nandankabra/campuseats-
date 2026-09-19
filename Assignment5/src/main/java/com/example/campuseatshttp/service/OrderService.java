package com.example.campuseatshttp.service;

import com.example.campuseatshttp.dto.CreateOrderRequest;
import com.example.campuseatshttp.dto.OrderResponse;
import com.example.campuseatshttp.model.Order;
import com.example.campuseatshttp.model.OrderStatus;
import com.example.campuseatshttp.store.OrderStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final OrderStore orderStore;

    private final AtomicLong sequence =
            new AtomicLong(1);

    public OrderService(OrderStore orderStore) {
        this.orderStore = orderStore;
    }

    public OrderResponse createOrder(
            CreateOrderRequest request,
            String idempotencyKey) {

        if (idempotencyKey != null) {

            Long existingId =
                    orderStore.findOrderByKey(
                            idempotencyKey);

            if (existingId != null) {

                return getOrder(existingId);
            }
        }

        Order order = new Order();

        order.setOrderId(
                sequence.getAndIncrement());

        order.setCustomerName(
                request.getCustomerName());

        order.setTotalAmount(
                request.getTotalAmount());

        order.setStatus(
                OrderStatus.CREATED);

        order.setCreatedAt(
                Instant.now());

        orderStore.save(order);

        if (idempotencyKey != null) {

            orderStore.saveKey(
                    idempotencyKey,
                    order.getOrderId());
        }

        return toResponse(order);
    }

    public OrderResponse getOrder(Long id) {

        Order order = orderStore
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Order not found"));

        return toResponse(order);
    }

    public List<Order> getAllOrders() {
        return orderStore.findAll();
    }

    public OrderResponse cancelOrder(Long id) {

        Order order = orderStore
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Order not found"));

        order.setStatus(
                OrderStatus.CANCELLED);

        return toResponse(order);
    }

    public void deleteOrder(Long id) {

        Order order = orderStore
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Order not found"));

        orderStore.delete(
                order.getOrderId());
    }

    private OrderResponse toResponse(
            Order order) {

        OrderResponse response =
                new OrderResponse();

        response.setOrderId(
                order.getOrderId());

        response.setCustomerName(
                order.getCustomerName());

        response.setTotalAmount(
                order.getTotalAmount());

        response.setStatus(
                order.getStatus().name());

        return response;
    }
}