package com.ecomert.controller;



import com.ecomert.model.Order;
import com.ecomert.model.OrderItem;
import com.ecomert.model.User;
import com.ecomert.service.OrderService;
import com.ecomert.service.impl.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrderService orderService;

    @GetMapping("")
    public ModelAndView listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "") String keyword) {

        ModelAndView modelAndView = new ModelAndView("admin/users/list");
        int size = 10;

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage;
        if (keyword.isEmpty()) {
            userPage = userService.findAll(pageable);
        } else {
            userPage = userService.findByKeyword(keyword, pageable);
        }

        modelAndView.addObject("users", userPage.getContent());
        modelAndView.addObject("currentPage", page);
        modelAndView.addObject("totalPages", userPage.getTotalPages());
        modelAndView.addObject("totalItems", userPage.getTotalElements());
        modelAndView.addObject("sortField", sortField);
        modelAndView.addObject("sortDir", sortDir);
        modelAndView.addObject("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        modelAndView.addObject("keyword", keyword);

        return modelAndView;
    }

    @GetMapping("/create")
    public ModelAndView showCreateForm() {
        ModelAndView modelAndView = new ModelAndView("admin/users/form");
        modelAndView.addObject("user", new User());
        modelAndView.addObject("roles", new String[]{"ROLE_USER", "ROLE_ADMIN"}); // Available roles
        return modelAndView;
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute User user,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", new String[]{"ROLE_USER", "ROLE_ADMIN"});
            return "admin/users/form";
        }

        try {
            // Mã hóa password trước khi lưu
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Đảm bảo role có prefix ROLE_
            if (!user.getRole().startsWith("ROLE_")) {
                user.setRole("ROLE_" + user.getRole());
            }

            userService.save(user);
            return "redirect:/admin/users?success=User created successfully";
        } catch (Exception e) {
            model.addAttribute("roles", new String[]{"ROLE_USER", "ROLE_ADMIN"});
            model.addAttribute("error", "Error creating user: " + e.getMessage());
            return "admin/users/form";
        }
    }

    @GetMapping("/edit/{id}")
    public ModelAndView showEditForm(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("admin/users/form");
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        modelAndView.addObject("user", user);
        modelAndView.addObject("roles", new String[]{"ROLE_USER", "ROLE_ADMIN"}); // Available roles
        return modelAndView;
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute User user,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", new String[]{"ROLE_USER", "ROLE_ADMIN"});
            return "admin/users/form";
        }

        try {
            User existingUser = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Cập nhật thông tin
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());

            // Chỉ cập nhật password nếu có nhập password mới
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            // Đảm bảo role có prefix ROLE_
            String role = user.getRole();
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }
            existingUser.setRole(role);

            existingUser.setEnabled(user.isEnabled());

            userService.save(existingUser);
            return "redirect:/admin/users?success=User updated successfully";
        } catch (Exception e) {
            model.addAttribute("roles", new String[]{"ROLE_USER", "ROLE_ADMIN"});
            model.addAttribute("error", "Error updating user: " + e.getMessage());
            return "admin/users/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/admin/users?success=User deleted successfully";
    }

    @GetMapping("/{id}")
    public String getUserDetail(@PathVariable Long id, Model model) {
        // Lấy thông tin user
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Lấy danh sách đơn hàng
        List<Order> orders = orderService.findByUser(user);

        // Tính toán thống kê
        long totalItems = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .mapToLong(OrderItem::getQuantity)
                .sum();

        double totalSpent = orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        // Thêm vào model
        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalSpent", totalSpent);

        return "admin/users/detail";
    }
}