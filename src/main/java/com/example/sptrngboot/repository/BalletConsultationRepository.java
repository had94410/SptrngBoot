package com.example.sptrngboot.repository;

import com.example.sptrngboot.model.BalletConsultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalletConsultationRepository extends JpaRepository<BalletConsultation, Long> {
    List<BalletConsultation> findAllByOrderByCreatedAtDesc();
}
