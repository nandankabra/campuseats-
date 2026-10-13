package com.example.campuseatshttp.controller;

import com.example.campuseatshttp.dto.CreateOrderRequest;
import com.example.campuseatshttp.dto.OrderResponse;
import com.example.campuseatshttp.dto.PatchOrderRequest;
import com.example.campuseatshttp.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/orders", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            OrderResponse existing = orderService.findOrderByIdempotencyKey(idempotencyKey);
            if (existing != null) {
                // Return 200 OK on idempotent replays
                return ResponseEntity.ok()
                        .location(URI.create("/orders/" + existing.getOrderId()))
                        .body(existing);
            }
        }

        OrderResponse response = orderService.createOrder(request, idempotencyKey);

        return ResponseEntity
                .created(URI.create("/orders/" + response.getOrderId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        OrderResponse response = orderService.getOrder(id);

        String etag = "\"" + Math.abs(response.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity
                    .status(304)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity
                .ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> patchOrder(
            @PathVariable Long id,
            @RequestBody PatchOrderRequest patch) {

        OrderResponse response = orderService.patchOrder(id, patch);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> options() {
        return ResponseEntity
                .noContent()
                .header("Allow", "GET,POST,PATCH,DELETE,OPTIONS")
                .build();
    }
}