package com.ecomert.controller;

import com.ecomert.model.Cart;
import com.ecomert.model.Order;
import com.ecomert.model.Product;
import com.ecomert.model.User;
import com.ecomert.service.IProductService;
import com.ecomert.service.OrderService;
import com.ecomert.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private Cart cart;

    @Autowired
    private IProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cart", cart);
        return "product/cart";
    }

    @PostMapping("/add/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(@PathVariable Long id) {
        try {
            Product product = productService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            cart.addProduct(product);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product added to cart");
            response.put("cartCount", cart.getItemCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @PathVariable Long id,
            @RequestParam int quantity) {
        cart.updateQuantity(id, quantity);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cart updated");
        response.put("cartCount", cart.getItemCount());
        response.put("total", cart.getTotal());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable Long id) {
        cart.removeProduct(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Product removed from cart");
        response.put("cartCount", cart.getItemCount());
        response.put("total", cart.getTotal());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/checkout")
    public String showCheckoutForm(Model model, Principal principal) {
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(
            @RequestParam String phoneNumber,
            @RequestParam String address,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime deliveryTime,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Order order = orderService.createOrder(user, cart, phoneNumber, address, deliveryTime);
            cart.getItems().clear(); // Clear cart after successful order

            redirectAttributes.addFlashAttribute("successMessage",
                    "Order placed successfully! Order ID: " + order.getId());
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error placing order: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }
}