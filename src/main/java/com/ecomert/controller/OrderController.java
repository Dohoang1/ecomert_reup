package com.ecomert.controller;

import com.ecomert.model.Order;
import com.ecomert.model.User;
import com.ecomert.service.OrderService;
import com.ecomert.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;

@Controller
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping("/orders/{id}")
    public String viewOrderDetail(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Kiểm tra quyền truy cập
        User currentUser = userService.getCurrentUser();
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to view this order");
        }

        model.addAttribute("order", order);
        model.addAttribute("isUserView", true);
        return "admin/orders/detail";
    }

    // Thêm endpoint cho user profile
    @GetMapping("/users/profile")
    public String viewProfile(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "users/profile";  // Tạo template này nếu chưa có
    }
}
