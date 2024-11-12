package com.ecomert.model;

public enum OrderStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    SHIPPING("Shipping"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayValue;

    OrderStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}