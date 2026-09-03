package com.example.campuseatsOrders.model;

import java.math.BigDecimal;

public class OrderItem {

    private final Long orderItemId;
    private final Long foodItemRef;
    private final String itemName;
    private final BigDecimal unitPrice;
    private final int quantity;
    private final BigDecimal lineTotal;

    public OrderItem(
            Long orderItemId,
            Long foodItemRef,
            String itemName,
            BigDecimal unitPrice,
            int quantity) {

        this.orderItemId = orderItemId;
        this.foodItemRef = foodItemRef;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal =
                unitPrice.multiply(
                        BigDecimal.valueOf(quantity));
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getFoodItemRef() {
        return foodItemRef;
    }

    public String getItemName() {
        return itemName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}