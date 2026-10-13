package com.example.campuseatshttp.exception;

public class ItemUnavailableException extends ApiException {
    public ItemUnavailableException(String itemName) {
        super("item-unavailable", "Item Unavailable", 409, "Item '" + itemName + "' is currently out of stock or unavailable");
    }
}
