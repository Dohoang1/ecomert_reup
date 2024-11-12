package com.ecomert.controller;

import com.ecomert.model.Order;
import com.ecomert.model.OrderStatus;
import com.ecomert.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "orderTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            Model model) {

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize,
                Sort.by(Sort.Direction.fromString(sortDir), sortField));

        Page<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderService.findByStatus(OrderStatus.valueOf(status), pageable);
        } else {
            orders = orderService.findAll(pageable);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", OrderStatus.values());

        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        model.addAttribute("order", order);
        return "admin/orders/detail";
    }

    @PostMapping("/{id}/ship")
    public String shipOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.shipOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + id + " has been shipped");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error shipping order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + id + " has been cancelled");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}