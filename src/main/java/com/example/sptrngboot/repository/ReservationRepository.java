package com.example.sptrngboot.repository;

import com.example.sptrngboot.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
