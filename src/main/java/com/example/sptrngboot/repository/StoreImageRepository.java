package com.example.sptrngboot.repository;

import com.example.sptrngboot.model.StoreImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {
    List<StoreImage> findByStoreId(Long storeId);
}