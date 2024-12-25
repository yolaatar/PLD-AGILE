package com.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "fast_tours") // Nom de la table associée
public class FastTour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Clé primaire avec auto-incrémentation
    @Column(name = "id") // Nom explicite de la colonne
    private long id;

    @Column(name = "start_id", nullable = false)
    private long startId;

    @Column(name = "pickup_id", nullable = false)
    private long pickupId;

    @Column(name = "dropoff_id", nullable = false)
    private long dropoffId;

    // Constructeur par défaut requis par JPA
    public FastTour() {}

    // Getters et Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStartId() {
        return startId;
    }

    public void setStartId(long startId) {
        this.startId = startId;
    }

    public long getPickupId() {
        return pickupId;
    }

    public void setPickupId(long pickupId) {
        this.pickupId = pickupId;
    }

    public long getDropoffId() {
        return dropoffId;
    }

    public void setDropoffId(long dropoffId) {
        this.dropoffId = dropoffId;
    }

    @Override
    public String toString() {
        return "FastTour{" +
                "id=" + id +
                ", startId=" + startId +
                ", pickupId=" + pickupId +
                ", dropoffId=" + dropoffId +
                '}';
    }
}
