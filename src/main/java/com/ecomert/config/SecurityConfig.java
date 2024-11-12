package com.ecomert.config;

import com.ecomert.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    @Autowired
    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập trang chính và xem sản phẩm
                        .requestMatchers("/products", "/products/{id}", "/", "/home").permitAll()
                        // Cho phép truy cập tài nguyên tĩnh và uploads - Sửa lại pattern matching
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**"
                        ).permitAll()
                        // Cho phép truy cập trang đăng nhập và đăng ký
                        .requestMatchers("/auth/**", "/error").permitAll()
                        // Chỉ admin mới có quyền truy cập
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers("/orders/**").authenticated()

                        .requestMatchers("/users/**").authenticated()

                        .requestMatchers("/users/profile").authenticated()

                        // Chỉ admin mới có quyền thêm/sửa/xóa sản phẩm
                        .requestMatchers(
                                "/products/create",
                                "/products/edit/**",
                                "/products/delete/**"
                        ).hasRole("ADMIN")
                        // Cart yêu cầu đăng nhập
                        .requestMatchers("/cart/**").authenticated()


                        // Các request khác yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/products")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/auth/login?logout")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.ecomert.model.User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().replace("ROLE_", ""))
                    .disabled(!user.isEnabled())
                    .build();
        };
    }
}