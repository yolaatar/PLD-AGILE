package com.example.controller;

import com.example.model.Courier;
import com.example.service.DataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/data")
public class DataController {
    private final DataService dataService;

    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping("/add/courier")
    public ResponseEntity<Courier> createCourier(@RequestBody Courier courier) {
        Courier savedCourier = dataService.addCourier(courier);
        return new ResponseEntity<>(savedCourier, HttpStatus.CREATED); // Retourne le courier avec un statut 201
    }

    @GetMapping("/get/allcourier")
    public ResponseEntity<List<Courier>> getAllCouriers() {
        List<Courier> couriers = dataService.getAllCouriers();
        return ResponseEntity.ok(couriers); // Retourne la liste avec un statut 200
    }
}
