package com.example.campuseatshttp.store;

import com.example.campuseatshttp.model.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderStore {

    private final Map<Long, Order> orders =
            new ConcurrentHashMap<>();

    private final Map<String, Long> idempotencyKeys =
            new ConcurrentHashMap<>();

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    public void save(Order order) {
        orders.put(order.getOrderId(), order);
    }

    public void delete(Long id) {
        orders.remove(id);
    }

    public Long findOrderByKey(String key) {
        return idempotencyKeys.get(key);
    }

    public void saveKey(String key, Long orderId) {
        idempotencyKeys.put(key, orderId);
    }
}