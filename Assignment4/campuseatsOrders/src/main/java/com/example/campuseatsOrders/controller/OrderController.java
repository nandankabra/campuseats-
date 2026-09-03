package com.example.campuseatsOrders.controller;

import com.example.campuseatsOrders.dto.CreateOrderRequest;
import com.example.campuseatsOrders.dto.OrderResponse;
import com.example.campuseatsOrders.model.OrderStatus;
import com.example.campuseatsOrders.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("Idempotency-Key")
            String idempotencyKey,

            @Valid
            @RequestBody
            CreateOrderRequest request) {

        OrderResponse response =
                orderService.createOrder(
                        request,
                        idempotencyKey);

        URI location =
                URI.create("/orders/" + response.orderId());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    /**
     * GET /orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.getOrder(id)
        );
    }

    /**
     * GET /orders?status=PLACED
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(required = false)
            OrderStatus status) {

        return ResponseEntity.ok(
                orderService.getOrders(status)
        );
    }

    /**
     * POST /orders/{id}/cancellation
     */
    @PostMapping("/{id}/cancellation")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id) {

        OrderResponse response =
                orderService.cancelOrder(id);

        return ResponseEntity
                .accepted()
                .body(response);
    }
}