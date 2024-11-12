package com.ecomert.controller.api;

import com.ecomert.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/pending-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getPendingOrdersCount() {
        long count = orderService.countPendingOrders();
        return ResponseEntity.ok(count);
    }
}