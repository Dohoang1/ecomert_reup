package com.ecomert.controller;

import com.ecomert.model.User;
import com.ecomert.service.impl.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "auth/login";  // Cập nhật đường dẫn template
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";  // Nếu có trang register, cũng cần di chuyển vào thư mục auth
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }
// Kiểm tra username đã tồn tại
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username already exists");
            return "auth/register";
        }
// Kiểm tra email đã tồn tại
        if (userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists");
            return "auth/register";
        }
// Mã hóa password và set role mặc định
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        userService.save(user);
        redirectAttributes.addAttribute("registered", true);
        return "redirect:/auth/login"; // Cập nhật URL redirect
    }
}