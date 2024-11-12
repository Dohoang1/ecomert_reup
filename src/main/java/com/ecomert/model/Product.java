package com.ecomert.model;


import jakarta.persistence.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String brand;
    private double price;
    private double stock;
    private String description;

    private String image;        // Tên file
    private String imagePath;    // Đường dẫn đầy đủ

    @Transient
    private MultipartFile imageFile;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Rating> ratings = new ArrayList<>();

    // Thêm các phương thức tiện ích
    public double getAverageRating() {
        if (ratings.isEmpty()) {
            return 0.0;
        }
        double sum = ratings.stream()
                .mapToInt(Rating::getScore)
                .sum();
        return Math.round((sum / ratings.size()) * 10.0) / 10.0;
    }

    public int getRatingCount() {
        return ratings.size();
    }
}
