package com.example.model;

import jakarta.persistence.*;

// Annotation pour indiquer que cette classe est une entité persistable
@Entity
@Table(name = "intersections") // Nom de la table associée
public class Intersection {

    // Clé primaire avec génération automatique
    @Id
    @Column(name = "id") // Nom de la colonne
    private long id;

    // Colonne latitude
    @Column(name = "latitude", nullable = false)
    private double latitude;

    // Colonne longitude
    @Column(name = "longitude", nullable = false)
    private double longitude;

    // Constructeur par défaut requis par JPA
    public Intersection() {}

    // Constructeur avec arguments pour usage pratique
    public Intersection(long id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters et setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Intersection{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
