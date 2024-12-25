package com.example.repository;

import com.example.model.DeliveryRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest, Long> {
}
