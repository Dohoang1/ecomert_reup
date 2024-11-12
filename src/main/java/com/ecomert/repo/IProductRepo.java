package com.ecomert.repo;

import com.ecomert.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProductRepo extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingOrBrandContaining(String name, String brand, Pageable pageable);
    @Query("SELECT DISTINCT p.brand FROM Product p ORDER BY p.brand")
    List<String> findDistinctBrands();

    Page<Product> findByBrand(String brand, Pageable pageable);
}
