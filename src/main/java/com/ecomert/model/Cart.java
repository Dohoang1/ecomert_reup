package com.ecomert.model;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class Cart {
    private Map<Long, CartItem> items = new HashMap<>();

    public void addProduct(Product product) {
        CartItem item = items.get(product.getId());
        if (item == null) {
            item = new CartItem();
            item.setId(product.getId());
            item.setProduct(product);
            item.setQuantity(1);
            items.put(product.getId(), item);
        } else {
            item.setQuantity(item.getQuantity() + 1);
        }
    }

    public void updateQuantity(Long productId, int quantity) {
        CartItem item = items.get(productId);
        if (item != null) {
            if (quantity > 0) {
                item.setQuantity(quantity);
            } else {
                items.remove(productId);
            }
        }
    }

    public void removeProduct(Long productId) {
        items.remove(productId);
    }

    public int getItemCount() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public double getTotal() {
        return items.values().stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }
}