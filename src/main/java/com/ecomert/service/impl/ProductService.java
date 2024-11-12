package com.ecomert.service.impl;

import com.ecomert.model.Product;
import com.ecomert.repo.IProductRepo;
import com.ecomert.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService implements IProductService {
    @Autowired
    private IProductRepo iProductRepo;

    @Override
    public Iterable<Product> findAll() {
        return iProductRepo.findAll();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return iProductRepo.findById(id);
    }

    @Override
    public void save(Product product) {
        iProductRepo.save(product);
    }

    @Override
    public void deleteById(Long id) {
        iProductRepo.deleteById(id);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return iProductRepo.findAll(pageable);
    }
    @Override
    public Page<Product> searchByNameOrBrand(String keyword, Pageable pageable) {
        return iProductRepo.findByNameContainingOrBrandContaining(keyword, keyword, pageable);
    }

    @Override
    public List<String> findAllBrands() {
        return iProductRepo.findDistinctBrands();
    }

    @Override
    public Page<Product> findByBrand(String brand, Pageable pageable) {
        return iProductRepo.findByBrand(brand, pageable);
    }
}
