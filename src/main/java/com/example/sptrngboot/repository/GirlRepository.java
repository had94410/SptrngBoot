package com.example.sptrngboot.repository;

import com.example.sptrngboot.model.Girl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GirlRepository extends JpaRepository<Girl, Long> {
    List<Girl> findByStoreId(Long storeId);
}
