package com.example.campuseatshttp.service;

import com.example.campuseatshttp.dto.CreateOrderRequest;
import com.example.campuseatshttp.dto.OrderItemDto;
import com.example.campuseatshttp.dto.OrderResponse;
import com.example.campuseatshttp.dto.PatchOrderRequest;
import com.example.campuseatshttp.exception.*;
import com.example.campuseatshttp.model.Order;
import com.example.campuseatshttp.model.OrderItem;
import com.example.campuseatshttp.model.OrderStatus;
import com.example.campuseatshttp.store.OrderStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderStore orderStore;
    private final AtomicLong sequence = new AtomicLong(1);

    public OrderService(OrderStore orderStore) {
        this.orderStore = orderStore;
    }

    public OrderResponse findOrderByIdempotencyKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        Long existingId = orderStore.findOrderByKey(key);
        if (existingId != null) {
            return getOrder(existingId);
        }
        return null;
    }

    public OrderResponse createOrder(CreateOrderRequest request, String idempotencyKey) {

        // Idempotency check: replay returns the existing resource
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            OrderResponse existing = findOrderByIdempotencyKey(idempotencyKey);
            if (existing != null) {
                return existing;
            }
        }

        // Empty cart check
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new EmptyCartException("Order cart cannot be empty. At least one item is required.");
        }

        // Check availability of each item
        for (OrderItemDto item : request.getItems()) {
            if (item.getName() != null &&
                    (item.getName().toUpperCase().contains("UNAVAILABLE") ||
                     item.getName().toUpperCase().contains("OUT_OF_STOCK"))) {
                throw new ItemUnavailableException(item.getName());
            }
        }

        // Check payment gateway simulation
        if (request.getPaymentMethod() != null &&
                ("DECLINED".equalsIgnoreCase(request.getPaymentMethod()) ||
                 "FAIL".equalsIgnoreCase(request.getPaymentMethod()))) {
            throw new PaymentDeclinedException("Card declined: insufficient funds or invalid card details");
        }

        // Risky work demonstration (B4: don't leak internals)
        if (Boolean.TRUE.equals(request.getSimulateInternalError())) {
            try {
                // Simulate an internal database deadlock or low-level connection failure
                throw new java.sql.SQLException("Connection to master database replica db-shard-01:5432 timed out [FATAL: 57P01 admin shutdown]");
            } catch (Exception ex) {
                log.error("Internal storage failure occurred during order creation: {}", ex.getMessage(), ex);
                throw new InternalServiceException("An unexpected internal error occurred while processing your order. Please try again later.");
            }
        }

        // Build domain order
        Order order = new Order();
        order.setOrderId(sequence.getAndIncrement());
        order.setCustomerName(request.getCustomerName());
        order.setAddress(request.getAddress());

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        List<OrderItem> domainItems = new ArrayList<>();
        for (OrderItemDto dto : request.getItems()) {
            BigDecimal price = dto.getPrice() != null ? dto.getPrice() : BigDecimal.valueOf(100);
            domainItems.add(new OrderItem(dto.getName(), dto.getQty(), price));
            calculatedTotal = calculatedTotal.add(price.multiply(BigDecimal.valueOf(dto.getQty())));
        }
        order.setItems(domainItems);

        if (request.getTotalAmount() != null) {
            order.setTotalAmount(request.getTotalAmount());
        } else {
            order.setTotalAmount(calculatedTotal);
        }

        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());

        orderStore.save(order);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            orderStore.saveKey(idempotencyKey, order.getOrderId());
        }

        return toResponse(order);
    }

    public OrderResponse getOrder(Long id) {
        Order order = orderStore.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderStore.findAll();
        List<OrderResponse> responses = new ArrayList<>();
        for (Order o : orders) {
            responses.add(toResponse(o));
        }
        return responses;
    }

    public OrderResponse patchOrder(Long id, PatchOrderRequest patch) {
        Order order = orderStore.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (patch.getAddress() != null && !patch.getAddress().isBlank()) {
            order.setAddress(patch.getAddress());
        }

        if (patch.getStatus() != null && !patch.getStatus().isBlank()) {
            OrderStatus newStatus;
            try {
                newStatus = OrderStatus.valueOf(patch.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ApiException("validation-error", "Invalid Status", 422,
                        "Unknown order status value: " + patch.getStatus());
            }

            // State transition validation
            if (order.getStatus() == OrderStatus.CANCELLED) {
                throw new IllegalTransitionException(order.getStatus().name(), newStatus.name());
            }
            if (order.getStatus() == OrderStatus.CONFIRMED && newStatus == OrderStatus.CREATED) {
                throw new IllegalTransitionException(order.getStatus().name(), newStatus.name());
            }

            order.setStatus(newStatus);
        }

        orderStore.save(order);
        return toResponse(order);
    }

    public OrderResponse cancelOrder(Long id) {
        Order order = orderStore.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalTransitionException("CANCELLED", "CANCELLED");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderStore.save(order);
        return toResponse(order);
    }

    public void deleteOrder(Long id) {
        Order order = orderStore.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        orderStore.delete(order.getOrderId());
    }

    public boolean isExistingOrder(Long id) {
        return orderStore.findById(id).isPresent();
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setCustomerName(order.getCustomerName());
        response.setAddress(order.getAddress());
        response.setItems(order.getItems());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        return response;
    }
}