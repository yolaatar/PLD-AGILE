package com.example.model;

import jakarta.persistence.*;

// Annotation pour indiquer que cette classe est une entité persistable
@Entity
@Table(name = "delivery_requests") // Nom de la table dans la base de données
public class DeliveryRequest {

    // Clé primaire avec génération automatique
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Nom de la colonne
    private int id;

    // Relation ManyToOne avec l'entité Intersection (pick-up point)
    @ManyToOne
    @JoinColumn(name = "pickup_intersection_id", nullable = false) // Clé étrangère pour le pick-up
    private Intersection pickup;

    // Relation ManyToOne avec l'entité Intersection (delivery point)
    @ManyToOne
    @JoinColumn(name = "delivery_intersection_id", nullable = false) // Clé étrangère pour la delivery
    private Intersection delivery;

    @Column(name = "pickup_duration")
    private double pickupDuration;

    @Column(name = "delivery_duration")
    private double deliveryDuration;
    public double getPickupDuration() {
        return pickupDuration;
    }
    public void setPickupDuration(double pickupDuration) {
        this.pickupDuration = pickupDuration;
    }
    public double getDeliveryDuration() {
        return deliveryDuration;
    }
    public void setDeliveryDuration(double deliveryDuration) {
        this.deliveryDuration = deliveryDuration;
    }
    // Constructeur par défaut requis par JPA
    public DeliveryRequest() {}

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Intersection getPickup() {
        return pickup;
    }

    public void setPickup(Intersection pickup) {
        this.pickup = pickup;
    }

    public Intersection getDelivery() {
        return delivery;
    }

    public void setDelivery(Intersection delivery) {
        this.delivery = delivery;
    }
}
