package com.example.campuseatsOrders.store;

import com.example.campuseatsOrders.model.Order;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OrderStore {

    private final Map<Long, Order> orders = new ConcurrentHashMap<>();

    private final Map<String, Long> idempotencyKeys =
            new ConcurrentHashMap<>();

    private final AtomicLong idGenerator =
            new AtomicLong(0);

    public Long nextId() {
        return idGenerator.incrementAndGet();
    }

    public void save(Order order) {
        orders.put(order.getOrderId(), order);
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    public Optional<Order> findByIdempotencyKey(String key) {

        Long orderId = idempotencyKeys.get(key);

        if (orderId == null) {
            return Optional.empty();
        }

        return findById(orderId);
    }

    public void saveIdempotencyKey(String key, Long orderId) {
        idempotencyKeys.put(key, orderId);
    }
}