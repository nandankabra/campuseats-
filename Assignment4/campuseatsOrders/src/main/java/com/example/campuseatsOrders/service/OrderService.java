package com.example.campuseatsOrders.service;

import com.example.campuseatsOrders.client.PaymentClient;
import com.example.campuseatsOrders.dto.CreateOrderRequest;
import com.example.campuseatsOrders.dto.OrderItemRequest;
import com.example.campuseatsOrders.dto.OrderResponse;
import com.example.campuseatsOrders.exception.OrderConflictException;
import com.example.campuseatsOrders.exception.OrderDomainException;
import com.example.campuseatsOrders.exception.OrderNotFoundException;
import com.example.campuseatsOrders.model.Order;
import com.example.campuseatsOrders.model.OrderItem;
import com.example.campuseatsOrders.model.OrderStatus;
import com.example.campuseatsOrders.store.OrderStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderStore orderStore;
    private final PaymentClient paymentClient;

    public OrderService(
            OrderStore orderStore,
            PaymentClient paymentClient) {

        this.orderStore = orderStore;
        this.paymentClient = paymentClient;
    }

    public OrderResponse createOrder(
            CreateOrderRequest request,
            String idempotencyKey) {

        validate(request, idempotencyKey);

        var existing =
                orderStore.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            return existing.get().asJson();
        }

        Long orderId = orderStore.nextId();

        List<OrderItem> items = request.items()
                .stream()
                .map(item ->
                        toOrderItem(item, orderId))
                .toList();

        BigDecimal calculatedTotal =
                items.stream()
                        .map(OrderItem::getLineTotal)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (request.totalAmount()
                .compareTo(calculatedTotal) != 0) {

            throw new OrderDomainException(
                    "totalAmount does not match the item total");
        }

        /*
         * Payments must succeed before the order is stored.
         */
        paymentClient.createPayment(
                orderId,
                idempotencyKey
        );

        Order order = new Order(
                orderId,
                request.userRef(),
                request.foodServiceRef(),
                request.totalAmount(),
                request.deliveryAddress(),
                OrderStatus.PENDING,
                items,
                idempotencyKey
        );

        orderStore.save(order);
        orderStore.saveIdempotencyKey(
                idempotencyKey,
                orderId
        );

        return order.asJson();
    }

    /*
     * C4:
     * Validate the complete request before accessing
     * request fields.
     */
    private void validate(
            CreateOrderRequest request,
            String idempotencyKey) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request body is required");
        }

        if (idempotencyKey == null ||
                idempotencyKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Idempotency-Key header is required");
        }

        if (request.userRef() == null ||
                request.userRef() <= 0) {

            throw new IllegalArgumentException(
                    "userRef must be positive");
        }

        if (request.foodServiceRef() == null ||
                request.foodServiceRef() <= 0) {

            throw new IllegalArgumentException(
                    "foodServiceRef must be positive");
        }

        if (request.totalAmount() == null ||
                request.totalAmount()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "totalAmount cannot be negative");
        }

        if (request.deliveryAddress() == null ||
                request.deliveryAddress().isBlank()) {

            throw new IllegalArgumentException(
                    "deliveryAddress is required");
        }

        if (request.items() == null ||
                request.items().isEmpty()) {

            throw new IllegalArgumentException(
                    "items must contain at least one item");
        }

        for (OrderItemRequest item : request.items()) {

            if (item == null) {
                throw new IllegalArgumentException(
                        "Order item cannot be null");
            }

            if (item.foodItemRef() == null ||
                    item.foodItemRef() <= 0) {

                throw new IllegalArgumentException(
                        "foodItemRef must be positive");
            }

            if (item.itemName() == null ||
                    item.itemName().isBlank()) {

                throw new IllegalArgumentException(
                        "itemName is required");
            }

            if (item.unitPrice() == null ||
                    item.unitPrice()
                            .compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "unitPrice cannot be negative");
            }

            if (item.quantity() <= 0) {

                throw new IllegalArgumentException(
                        "quantity must be greater than zero");
            }
        }
    }

    private OrderItem toOrderItem(
            OrderItemRequest request,
            Long orderId) {

        /*
         * orderId is an internal relationship and is deliberately
         * not exposed in OrderItemResponse.
         */
        long itemId =
                orderId * 1000L + request.foodItemRef();

        return new OrderItem(
                itemId,
                request.foodItemRef(),
                request.itemName(),
                request.unitPrice(),
                request.quantity()
        );
    }

    public OrderResponse getOrder(Long id) {

        Order order = orderStore.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id));

        return order.asJson();
    }

    public List<OrderResponse> getOrders(
            OrderStatus status) {

        return orderStore.findAll()
                .stream()
                .filter(order ->
                        status == null ||
                                order.getStatus() == status)
                .map(Order::asJson)
                .toList();
    }

    public OrderResponse cancelOrder(Long id) {

        Order order = orderStore.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id));

        if (order.getStatus() == OrderStatus.CANCELLED) {

            throw new OrderConflictException(
                    "Order " + id +
                            " is already cancelled");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {

            throw new OrderConflictException(
                    "Completed order " + id +
                            " cannot be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        orderStore.save(order);

        return order.asJson();
    }
}