package com.example.service;

import com.example.model.Courier;
import com.example.model.Tour;
import com.example.repository.CourierRepository;
import com.example.repository.TourRepository;
import org.apache.coyote.Response;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DataService {
    private final CourierRepository courierRepository;


    public DataService(CourierRepository courierRepository, TourRepository tourRepository) {
        this.courierRepository = courierRepository;
    }

    public Courier addCourier(Courier courier) {
        return courierRepository.save(courier);
    }

    public List<Courier> getAllCouriers() {
        return courierRepository.findAll();
    }

    public Optional<Courier> getCourierById(int id) {
        return courierRepository.findById(id) ;
    }
}
