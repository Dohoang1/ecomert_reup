package com.ecomert.service;

import com.ecomert.model.*;
import com.ecomert.repo.IProductRepo;
import com.ecomert.repo.OrderRepository;
import com.ecomert.service.impl.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private IProductRepo productRepository;

    public Order createOrder(User user, Cart cart, String phoneNumber, String address, LocalDateTime deliveryTime) {
        Order order = new Order();
        order.setUser(user);
        order.setPhoneNumber(phoneNumber);
        order.setAddress(address);
        order.setDeliveryTime(deliveryTime);
        order.setTotalAmount(cart.getTotal());

        // Convert cart items to order items
        for (Map.Entry<Long, CartItem> entry : cart.getItems().entrySet()) {
            CartItem cartItem = entry.getValue();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            order.getItems().add(orderItem);
        }

        return orderRepository.save(order);
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByOrderTimeDesc(user);
    }

    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public void shipOrder(Long id) {
        Order order = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order must be in PENDING state to be shipped");
        }

        // Kiểm tra và cập nhật stock cho từng sản phẩm
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int requestedQuantity = item.getQuantity();

            // Kiểm tra xem còn đủ hàng không
            if (product.getStock() < requestedQuantity) {
                throw new IllegalStateException(
                        "Not enough stock for product: " + product.getName() +
                                ". Available: " + product.getStock() +
                                ", Requested: " + requestedQuantity
                );
            }

            // Cập nhật số lượng tồn kho
            product.setStock(product.getStock() - requestedQuantity);
            productRepository.save(product);
        }

        // Cập nhật trạng thái đơn hàng
        order.setStatus(OrderStatus.SHIPPING);
        orderRepository.save(order);
    }

    public void cancelOrder(Long id) {
        Order order = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel order in " + order.getStatus() + " state");
        }

        // Nếu đơn hàng đang ở trạng thái SHIPPING, hoàn lại số lượng stock
        if (order.getStatus() == OrderStatus.SHIPPING) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public long countPendingOrders() {
        return orderRepository.countByStatus(OrderStatus.PENDING);
    }

    public List<Order> findByUser(User user) {
        return orderRepository.findByUserOrderByOrderTimeDesc(user);
    }

    public long calculateTotalItemsByUser(User user) {
        return findByUser(user).stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .flatMap(order -> order.getItems().stream())
                .mapToLong(OrderItem::getQuantity)
                .sum();
    }

    public double calculateTotalSpentByUser(User user) {
        return findByUser(user).stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    public List<OrderItem> findAllOrderItemsByUser(User user) {
        return findByUser(user).stream()
                .flatMap(order -> order.getItems().stream())
                .toList();
    }

    /**
     * Đánh dấu đơn hàng đã giao
     */
    public void markAsDelivered(Long id) {
        Order order = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.SHIPPING) {
            throw new IllegalStateException("Order must be in SHIPPING state to be delivered");
        }

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }

    /**
     * Lấy số lượng đơn hàng theo từng trạng thái của user
     */
    public Map<OrderStatus, Long> getOrderStatusCountsByUser(User user) {
        return findByUser(user).stream()
                .collect(Collectors.groupingBy(
                        Order::getStatus,
                        Collectors.counting()
                ));
    }

    /**
     * Tính tổng doanh thu từ các đơn hàng đã giao
     */
    public double calculateTotalRevenue() {
        return orderRepository.findByStatus(OrderStatus.DELIVERED, Pageable.unpaged())
                .stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }
}