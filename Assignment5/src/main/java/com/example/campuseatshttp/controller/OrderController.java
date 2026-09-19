package com.example.campuseatshttp.controller;

import com.example.campuseatshttp.dto.CreateOrderRequest;
import com.example.campuseatshttp.dto.OrderResponse;
import com.example.campuseatshttp.model.Order;
import com.example.campuseatshttp.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(
            OrderService orderService) {

        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid
            @RequestBody
            CreateOrderRequest request,

            @RequestHeader(
                    value = "Idempotency-Key",
                    required = false)
            String idempotencyKey) {

        OrderResponse response =
                orderService.createOrder(
                        request,
                        idempotencyKey);

        return ResponseEntity
                .created(
                        URI.create(
                                "/orders/" +
                                        response.getOrderId()))
                .header(
                        "X-RateLimit-Limit",
                        "100")
                .header(
                        "X-RateLimit-Remaining",
                        "99")
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id,

            @RequestHeader(
                    value = "If-None-Match",
                    required = false)
            String ifNoneMatch) {

        OrderResponse response =
                orderService.getOrder(id);

        String etag =
                "\"" +
                        response.hashCode() +
                        "\"";

        if (etag.equals(ifNoneMatch)) {

            return ResponseEntity
                    .status(304)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity
                .ok()
                .eTag(etag)
                .cacheControl(
                        CacheControl.maxAge(
                                60,
                                TimeUnit.SECONDS))
                .body(response);
    }

    @GetMapping
    public List<Order> getOrders() {

        return orderService.getAllOrders();
    }

    @PostMapping("/{id}/cancellation")
    public ResponseEntity<OrderResponse>
    cancelOrder(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.cancelOrder(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long id) {

        orderService.deleteOrder(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @RequestMapping(
            method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> options() {

        return ResponseEntity
                .noContent()
                .header(
                        "Allow",
                        "GET,POST,DELETE,OPTIONS")
                .build();
    }
}