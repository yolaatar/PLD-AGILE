package com.example.repository;

import com.example.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierRepository extends JpaRepository<Courier, Integer> {
}
