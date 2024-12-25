package com.example.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name ="tours" )
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false) // Clé étrangère pour la delivery
    private Intersection warehouse;

    @ManyToOne
    @JoinColumn(name = "courier_id", nullable = true)
    private Courier courier;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tour_id") // Clé étrangère dans la table DeliveryRequest
    private List<DeliveryRequest> deliveryRequests = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Intersection getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Intersection warehouse) {
        this.warehouse = warehouse;
    }

    public List<DeliveryRequest> getDeliveryRequests() {
        return deliveryRequests;
    }

    public void setDeliveryRequests(List<DeliveryRequest> deliveryRequests) {
        this.deliveryRequests = deliveryRequests;
    }

    public Courier getCourier() {
        return courier;
    }

    public void setCourier(Courier courier) {
        this.courier = courier;
    }
}
