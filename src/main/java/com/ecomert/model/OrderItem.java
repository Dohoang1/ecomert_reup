package com.ecomert.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;
    private double price; // Giá tại thời điểm đặt hàng

    // Helper methods
    public double getSubtotal() {
        return price * quantity;
    }

    // Factory method
    public static OrderItem createFrom(Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPrice(product.getPrice()); // Lưu giá tại thời điểm đặt hàng
        return item;
    }
}