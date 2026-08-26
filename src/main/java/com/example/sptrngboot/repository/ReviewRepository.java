package com.example.sptrngboot.repository;

import com.example.sptrngboot.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByStoreIdOrderByCreatedAtDesc(Long storeId);
    long countByStoreId(Long storeId);
}
