package com.ecomert.service;

import com.ecomert.model.Product;
import com.ecomert.model.Rating;
import com.ecomert.model.User;
import com.ecomert.repo.RatingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class RatingService {
    @Autowired
    private RatingRepository ratingRepository;

    public Rating saveRating(Rating rating) {
        return ratingRepository.save(rating);
    }

    public Optional<Rating> findByProductAndUser(Product product, User user) {
        return ratingRepository.findByProductAndUser(product, user);
    }

    public boolean hasUserRated(Product product, User user) {
        return ratingRepository.existsByProductAndUser(product, user);
    }
}