package com.ecomert.model;

import lombok.Data;

@Data
public class CartItem {
    private Long id;
    private Product product;
    private int quantity;

    public double getSubtotal() {
        return product.getPrice() * quantity;
    }
}