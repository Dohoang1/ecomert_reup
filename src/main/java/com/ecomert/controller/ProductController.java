package com.ecomert.controller;

import com.ecomert.model.Product;
import com.ecomert.model.Rating;
import com.ecomert.model.User;
import com.ecomert.service.IProductService;
import com.ecomert.service.RatingService;
import com.ecomert.service.impl.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("")
public class ProductController {
    @Autowired
    private IProductService iProductService;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/products")
    public ModelAndView listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String brand) {

        ModelAndView modelAndView = new ModelAndView("product/list");
        int size = 6; // Cố định số sản phẩm mỗi trang

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage;
        if (!keyword.isEmpty()) {
            productPage = iProductService.searchByNameOrBrand(keyword, pageable);
        } else if (brand != null && !brand.isEmpty()) {
            productPage = iProductService.findByBrand(brand, pageable);
        } else {
            productPage = iProductService.findAll(pageable);
        }

        // Get all unique brands for filter
        List<String> brands = iProductService.findAllBrands();

        modelAndView.addObject("products", productPage.getContent());
        modelAndView.addObject("currentPage", page);
        modelAndView.addObject("totalPages", productPage.getTotalPages());
        modelAndView.addObject("totalItems", productPage.getTotalElements());
        modelAndView.addObject("sortField", sortField);
        modelAndView.addObject("sortDir", sortDir);
        modelAndView.addObject("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        modelAndView.addObject("keyword", keyword);
        modelAndView.addObject("brands", brands);
        modelAndView.addObject("selectedBrand", brand);

        return modelAndView;
    }

    @GetMapping("/products/create")
    public ModelAndView showCreateForm() {
        ModelAndView modelAndView = new ModelAndView("/product/create");
        modelAndView.addObject("product", new Product());
        return modelAndView;
    }

    @PostMapping("/products/create")
    public ModelAndView saveProduct(@ModelAttribute("product") Product product, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("/product/create");

        try {
            MultipartFile multipartFile = product.getImageFile();
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String fileName = multipartFile.getOriginalFilename();
                String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

                File file = new File(uploadPath);
                if (!file.exists()) {
                    file.mkdirs();
                }

                Path path = Paths.get(uploadPath, uniqueFileName);
                Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                product.setImage(uniqueFileName);

                String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                String fullPath = baseUrl + "/uploads/images/" + uniqueFileName;
                product.setImagePath(fullPath);
            }

            iProductService.save(product);
            modelAndView.addObject("product", new Product());
            modelAndView.addObject("successMessage", "Product created successfully!");

        } catch (Exception e) {
            modelAndView.addObject("product", product);
            modelAndView.addObject("errorMessage", "Error uploading file: " + e.getMessage());
        }

        return modelAndView;
    }

    @GetMapping("/products/{id}")
    public ModelAndView showDetail(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("product/detail");
        Product product = iProductService.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        modelAndView.addObject("product", product);
        return modelAndView;
    }

    @GetMapping("/products/edit/{id}")
    public ModelAndView showEditForm(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("product/edit");
        Product product = iProductService.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        modelAndView.addObject("product", product);
        return modelAndView;
    }

    @PostMapping("/products/edit/{id}")
    public ModelAndView updateProduct(@PathVariable Long id, @ModelAttribute Product product, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("product/edit");

        try {
            Product existingProduct = iProductService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Cập nhật thông tin cơ bản
            existingProduct.setName(product.getName());
            existingProduct.setBrand(product.getBrand());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setStock(product.getStock());
            existingProduct.setDescription(product.getDescription());

            // Xử lý upload file mới nếu có
            MultipartFile multipartFile = product.getImageFile();
            if (multipartFile != null && !multipartFile.isEmpty()) {
                // Xóa file cũ nếu có
                if (existingProduct.getImage() != null) {
                    Path oldFilePath = Paths.get(uploadPath, existingProduct.getImage());
                    Files.deleteIfExists(oldFilePath);
                }

                // Lưu file mới
                String fileName = multipartFile.getOriginalFilename();
                String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

                Path path = Paths.get(uploadPath, uniqueFileName);
                Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                existingProduct.setImage(uniqueFileName);

                String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                String fullPath = baseUrl + "/uploads/images/" + uniqueFileName;
                existingProduct.setImagePath(fullPath);
            }

            iProductService.save(existingProduct);
            modelAndView.addObject("successMessage", "Product updated successfully!");
            modelAndView.addObject("product", existingProduct);

        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "Error updating product: " + e.getMessage());
            modelAndView.addObject("product", product);
        }

        return modelAndView;
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        try {
            Product product = iProductService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Xóa file ảnh nếu có
            if (product.getImage() != null) {
                Path filePath = Paths.get(uploadPath, product.getImage());
                Files.deleteIfExists(filePath);
            }

            iProductService.deleteById(id);
            return "redirect:/products?success=Product deleted successfully";
        } catch (Exception e) {
            return "redirect:/products?error=Error deleting product: " + e.getMessage();
        }
    }

    @Autowired
    private RatingService ratingService;

    @Autowired
    private UserService userService;

    @PostMapping("/products/{id}/rate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rateProduct(
            @PathVariable Long id,
            @RequestParam int score,
            @RequestParam(required = false) String comment,
            Principal principal) {

        try {
            Product product = iProductService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Rating rating = ratingService.findByProductAndUser(product, user)
                    .orElse(new Rating());

            rating.setProduct(product);
            rating.setUser(user);
            rating.setScore(score);
            rating.setComment(comment);

            ratingService.saveRating(rating);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("averageRating", product.getAverageRating());
            response.put("ratingCount", product.getRatingCount());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}